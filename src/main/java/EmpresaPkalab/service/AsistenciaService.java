package EmpresaPkalab.service;

import EmpresaPkalab.dto.MarcadoRequest;
import EmpresaPkalab.model.Asistencia;
import EmpresaPkalab.model.Horario;
import EmpresaPkalab.model.Usuario;
import EmpresaPkalab.repository.AsistenciaRepository;
import EmpresaPkalab.repository.HorarioRepository;
import EmpresaPkalab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioRepository horarioRepo;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public Asistencia registrarEntrada(MarcadoRequest request) {
        // 1. Buscamos al motorizado
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscamos el Horario asignado (el ID que viene de la App de Android)
        Horario horario = horarioRepo.findById(request.getRequerimientoId())
                .orElseThrow(() -> new RuntimeException("No se encontró la asignación de horario"));

        // 3. Procesamos la ubicación GPS del celular
        Point puntoMovil = geometryFactory.createPoint(new Coordinate(request.getLongitud(), request.getLatitud()));

        // 4. Creamos el objeto Asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setHorario(horario); // Vinculamos directamente al modelo Horario
        asistencia.setHoraEntrada(LocalDateTime.now());
        asistencia.setUbicacionMarcado(puntoMovil);

        // 5. Validación de cercanía con la Tienda del Horario
        if (horario.getTienda() != null && horario.getTienda().getUbicacion() != null) {
            Point ubicacionTienda = horario.getTienda().getUbicacion();

            // Usamos el radio personalizado de tu tabla 'tienda'
            int radioPermitido = (horario.getTienda().getRadioPermitidoMetros() != null)
                    ? horario.getTienda().getRadioPermitidoMetros()
                    : 100;

            // Calculamos distancia real (aproximación en metros)
            double distanciaMetros = puntoMovil.distance(ubicacionTienda) * 111319.9;

            if (distanciaMetros <= radioPermitido) {
                asistencia.setEsValida(true);
                asistencia.setObservacion("Marcado válido a " + (int)distanciaMetros + "m.");
            } else {
                asistencia.setEsValida(false);
                asistencia.setObservacion("Fuera de rango: " + (int)distanciaMetros + "m. (Máximo: " + radioPermitido + "m)");
            }
        } else {
            asistencia.setEsValida(false);
            asistencia.setObservacion("Error: La tienda no tiene coordenadas configuradas.");
        }

        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public Asistencia registrarSalida(UUID asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("No se encontró el registro de entrada"));

        asistencia.setHoraSalida(LocalDateTime.now());
        return asistenciaRepository.save(asistencia);
    }

    public Map<String, Object> obtenerAsistenciaDia(UUID usuarioId, UUID horarioId) {
        // Buscamos si ya existe asistencia para este usuario y este horario
        return asistenciaRepository.findByUsuarioIdAndHorarioId(usuarioId, horarioId)
                .map(a -> {
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("asistenciaId", a.getId());
                    resultado.put("marcoEntrada", true);
                    resultado.put("marcoSalida", a.getHoraSalida() != null);
                    return resultado;
                })
                .orElse(Map.of("marcoEntrada", false));
    }

    // --- NUEVOS MÉTODOS PARA EL PANEL ADMINISTRADOR ---

    /**
     * Lista todas las asistencias para el administrador.
     * Ideal para una tabla general.
     */
    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }

    /**
     * Lista solo las alertas (marcados fuera de rango).
     * Útil para notificaciones en el panel.
     */
    public List<Asistencia> listarAlertasFueraDeRango() {
        return asistenciaRepository.findByEsValidaFalseOrderByHoraEntradaDesc();
    }

    /**
     * Reporte por fechas.
     */
    public List<Asistencia> obtenerReportePorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return asistenciaRepository.findByHoraEntradaBetweenOrderByHoraEntradaAsc(inicio, fin);
    }
}