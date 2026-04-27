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
            // El service ahora busca internamente en la tabla 'horario'
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

    /**
     * Consultar si el motorizado ya marcó hoy.
     * Cambiamos el nombre del PathVariable de 'requerimientoId' a 'horarioId'
     * para que coincida con lo que espera el nuevo Service.
     */
    @GetMapping("/estado-hoy/{usuarioId}/{horarioId}")
    public ResponseEntity<?> consultarEstadoHoy(
            @PathVariable UUID usuarioId,
            @PathVariable UUID horarioId) {
        try {
            // El service ahora usa findByUsuarioIdAndHorarioId
            return ResponseEntity.ok(asistenciaService.obtenerAsistenciaDia(usuarioId, horarioId));
        } catch (Exception e) {
            // Si hay error, asumimos que no hay asistencia previa
            return ResponseEntity.ok(Map.of("marcoEntrada", false));
        }
    }

    // --- ENDPOINTS PARA EL PANEL DE ADMINISTRADOR (REACT) ---

    @GetMapping("/admin/lista-completa")
    public ResponseEntity<List<Asistencia>> obtenerTodas() {
        return ResponseEntity.ok(asistenciaService.listarTodas());
    }

    @GetMapping("/admin/reporte")
    public ResponseEntity<List<Asistencia>> obtenerReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // Convertimos LocalDate a LocalDateTime para tu service
        return ResponseEntity.ok(asistenciaService.obtenerReportePorFechas(
                inicio.atStartOfDay(),
                fin.atTime(23, 59, 59)
        ));
    }

    @GetMapping("/admin/alertas")
    public ResponseEntity<List<Asistencia>> obtenerAlertas() {
        // Llama al método que creamos en el service para esValida = false
        return ResponseEntity.ok(asistenciaService.listarAlertasFueraDeRango());
    }
}