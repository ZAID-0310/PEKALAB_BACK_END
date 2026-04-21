package EmpresaPkalab.controller;

import EmpresaPkalab.dto.MarcadoRequest;
import EmpresaPkalab.model.Asistencia;
import EmpresaPkalab.service.AsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @PostMapping("/marcar-entrada")
    public ResponseEntity<?> marcarEntrada(@RequestBody MarcadoRequest request) {
        try {
            Asistencia asistencia = asistenciaService.registrarEntrada(request);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Asistencia procesada",
                    "id", asistencia.getId(),
                    "esValida", asistencia.getEsValida(),
                    "observacion", asistencia.getObservacion(),
                    "hora", asistencia.getHoraEntrada()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se pudo registrar la asistencia",
                    "detalle", e.getMessage()
            ));
        }
    }

    @PostMapping("/marcar-salida/{asistenciaId}")
    public ResponseEntity<?> marcarSalida(@PathVariable UUID asistenciaId) {
        try {
            Asistencia asistencia = asistenciaService.registrarSalida(asistenciaId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Salida registrada correctamente",
                    "horaSalida", asistencia.getHoraSalida()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/estado-hoy/{usuarioId}/{requerimientoId}")
    public ResponseEntity<?> consultarEstadoHoy(
            @PathVariable UUID usuarioId,
            @PathVariable UUID requerimientoId) {
        try {
            return ResponseEntity.ok(asistenciaService.obtenerAsistenciaDia(usuarioId, requerimientoId));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("asistencia", "ninguna"));
        }
    }

    // --- NUEVOS ENDPOINTS PARA EL PANEL DE ADMINISTRADOR (REACT) ---

    /**
     * Reporte General por rango de fechas
     * URL de ejemplo: /api/asistencia/reporte?inicio=2026-04-01&fin=2026-04-30
     */
    @GetMapping("/reporte")
    public ResponseEntity<List<Asistencia>> obtenerReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(asistenciaService.obtenerReporteAsistencias(inicio, fin));
    }

    /**
     * Reporte de Alertas (Solo marcaciones fuera de rango GPS)
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<Asistencia>> obtenerAlertas() {
        return ResponseEntity.ok(asistenciaService.obtenerAlertasGeograficas());
    }
}