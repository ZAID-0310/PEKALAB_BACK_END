package EmpresaPkalab.service;

import EmpresaPkalab.dto.MarcadoRequest;
import EmpresaPkalab.model.Asistencia;
import EmpresaPkalab.model.RequerimientoTienda; // Tu modelo
import EmpresaPkalab.model.Usuario;
import EmpresaPkalab.repository.AsistenciaRepository;
import EmpresaPkalab.repository.RequerimientoRepository; // Tu repositorio
import EmpresaPkalab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final RequerimientoRepository requerimientoRepo; // Inyectado correctamente

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public Asistencia registrarEntrada(MarcadoRequest request) {
        // 1. Buscamos al usuario usando el UUID que viene en el DTO
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscamos el RequerimientoTienda usando el UUID
        // Aquí usamos requerimientoRepo que maneja la entidad RequerimientoTienda
        RequerimientoTienda req = requerimientoRepo.findById(request.getRequerimientoId())
                .orElseThrow(() -> new RuntimeException("Requerimiento no encontrado"));

        // 3. Procesamos la ubicación PostGIS
        Point puntoMovil = geometryFactory.createPoint(new Coordinate(request.getLongitud(), request.getLatitud()));

        // 4. Creamos la asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setRequerimientoTienda(req);
        asistencia.setHoraEntrada(LocalDateTime.now());
        asistencia.setUbicacionMarcado(puntoMovil);

        // 5. Validación de cercanía con la Tienda
        // Accedemos a la ubicación fija de la tienda vinculada al requerimiento
        if (req.getTienda() != null && req.getTienda().getUbicacion() != null) {
            double distanciaMetros = puntoMovil.distance(req.getTienda().getUbicacion()) * 111319.9;

            if (distanciaMetros <= 200) { // Tolerancia de 200 metros
                asistencia.setEsValida(true);
                asistencia.setObservacion("Marcado válido a " + (int)distanciaMetros + "m.");
            } else {
                asistencia.setEsValida(false);
                asistencia.setObservacion("Fuera de rango por " + (int)distanciaMetros + "m.");
            }
        } else {
            asistencia.setEsValida(false);
            asistencia.setObservacion("Error: La tienda no tiene coordenadas.");
        }

        return asistenciaRepository.save(asistencia);
    }

    // Método para registrar la salida
    @Transactional
    public Asistencia registrarSalida(UUID asistenciaId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("No se encontró el registro de entrada"));

        asistencia.setHoraSalida(LocalDateTime.now());
        return asistenciaRepository.save(asistencia);
    }

    // Método para verificar si ya marcó (Útil para el flujo del App)
    public Map<String, Object> obtenerAsistenciaDia(UUID usuarioId, UUID reqId) {
        return asistenciaRepository.findByUsuarioIdAndRequerimientoTiendaId(usuarioId, reqId)
                .map(a -> {
                    // Creamos el mapa explícitamente como <String, Object>
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("asistenciaId", a.getId());
                    resultado.put("marcoEntrada", true);
                    resultado.put("marcoSalida", a.getHoraSalida() != null);
                    return resultado;
                })
                .orElse(Map.of("marcoEntrada", false));
    }
    /**
     * GENERAR REPORTE DE ASISTENCIAS (Planificado vs Real)
     * Este método servirá para tu nueva pantalla de reportes en React.
     */
    public List<Asistencia> obtenerReporteAsistencias(LocalDate inicio, LocalDate fin) {
        // Convertimos LocalDate a LocalDateTime (inicio y fin del día)
        LocalDateTime fechaInicio = inicio.atStartOfDay();
        LocalDateTime fechaFin = fin.atTime(23, 59, 59);

        // Usamos el nuevo método del repositorio que creamos
        return asistenciaRepository.findByHoraEntradaBetweenOrderByHoraEntradaAsc(fechaInicio, fechaFin);
    }

    /**
     * REPORTE ESPECÍFICO DE ALERTAS
     * Filtra solo las asistencias que fueron marcadas fuera de rango.
     */
    public List<Asistencia> obtenerAlertasGeograficas() {
        return asistenciaRepository.findByEsValidaFalseOrderByHoraEntradaDesc();
    }
}