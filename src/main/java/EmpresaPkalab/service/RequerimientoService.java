package EmpresaPkalab.service;

import EmpresaPkalab.model.RequerimientoTienda;
import EmpresaPkalab.model.Tienda;
import EmpresaPkalab.repository.RequerimientoRepository;
import EmpresaPkalab.repository.TiendaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequerimientoService {

    private final RequerimientoRepository requerimientoRepository;
    private final TiendaRepository tiendaRepository;

    public List<RequerimientoTienda> listarTodo() {
        return requerimientoRepository.findAll();
    }

    @Transactional
    public void importarDesdeExcel(MultipartFile archivo) throws Exception {
        Workbook workbook = new XSSFWorkbook(archivo.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);


        requerimientoRepository.deleteAllInBatch();
        requerimientoRepository.flush();

        List<Tienda> todasLasTiendas = tiendaRepository.findAll();


        Map<String, Integer> contadoresCoches = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) continue;

            try {

                String nombreExcel = row.getCell(0).getStringCellValue().trim();
                Tienda tienda = todasLasTiendas.stream()
                        .filter(t -> normalizar(t.getNombreTienda()).equalsIgnoreCase(normalizar(nombreExcel)))
                        .findFirst()
                        .orElse(null);

                if (tienda == null) {
                    System.out.println("Tienda no encontrada: " + nombreExcel);
                    continue;
                }


                LocalDate fecha = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
                LocalTime inicio = leerHoraSegura(row.getCell(3));
                LocalTime fin = leerHoraSegura(row.getCell(4));
                int cantidad = (int) row.getCell(5).getNumericCellValue();

                // 3. GENERAR LLAVE PARA EL CONTADOR
                String llaveUnica = tienda.getId() + "-" + fecha + "-" + inicio;
                int correlativoActual = contadoresCoches.getOrDefault(llaveUnica, 0);

                // 4. CREAR REGISTROS INDIVIDUALES
                for (int j = 1; j <= cantidad; j++) {
                    RequerimientoTienda req = new RequerimientoTienda();
                    req.setTienda(tienda);
                    req.setFecha(fecha);
                    req.setDiaSemana(obtenerNombreDia(fecha));
                    req.setHoraInicio(inicio);
                    req.setHoraFin(fin);

                    // Asigna el número que sigue (1, 2, 3...)
                    req.setNMotorizado(correlativoActual + j);

                    req.setEstado("PENDIENTE");
                    requerimientoRepository.save(req);
                }

                // Actualizar el contador para esa hora específica
                contadoresCoches.put(llaveUnica, correlativoActual + cantidad);

            } catch (Exception e) {
                System.err.println("Error procesando fila " + (i + 1) + ": " + e.getMessage());
            }
        }
        workbook.close();
    }

    // Auxiliar: Normaliza texto (p.ej. "Canadá" -> "canada")
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase().trim();
    }

    // Auxiliar: Lee horas de Excel ya sean formato Hora o Texto
    private LocalTime leerHoraSegura(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getLocalDateTimeCellValue().toLocalTime().withSecond(0).withNano(0);
        }
        return LocalTime.parse(cell.getStringCellValue());
    }

    private String obtenerNombreDia(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
    public List<RequerimientoTienda> obtenerHorario(UUID usuarioId) {
        return requerimientoRepository.buscarHorarioMotorizado(usuarioId);
    }
}