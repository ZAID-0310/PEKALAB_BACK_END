package EmpresaPkalab.controller;

import EmpresaPkalab.model.Horario;
import EmpresaPkalab.service.HorarioService;
import EmpresaPkalab.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;
    private final UsuarioService usuarioService; // <--- AGREGA ESTA LÍNEA
    /**
     * ASIGNACIÓN AUTOMÁTICA (Gatillo)
     * Ejecuta la lógica de cercanía geográfica para todos los motorizados.
     */
    @PostMapping("/generar-automatica")
    public ResponseEntity<String> generarAsignaciones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio) {
        horarioService.procesarAsignacionesSemanales(fechaInicio);
        return ResponseEntity.ok("Asignaciones generadas exitosamente por cercanía geográfica.");
    }

    // Este para la pantalla principal (Solo lo nuevo)
    @GetMapping("/proximos/{usuarioId}")
    public ResponseEntity<?> obtenerProximos(@PathVariable UUID usuarioId) {
        if (!esUsuarioAutorizado(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para ver los horarios de otro usuario.");
        }
        return ResponseEntity.ok(horarioService.obtenerProximos(usuarioId));
    }

    // Este para la nueva sección de Historial
    @GetMapping("/historial/{usuarioId}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable UUID usuarioId) {
        if (!esUsuarioAutorizado(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para ver el historial de otro usuario.");
        }
        return ResponseEntity.ok(horarioService.obtenerHistorial(usuarioId));
    }

    private boolean esUsuarioAutorizado(UUID usuarioIdEnUrl) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailToken = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMINISTRADOR"));

        if (isAdmin) return true;

        // Ahora que inyectamos el service, esta línea funcionará:
        UUID idRealToken = usuarioService.obtenerIdPorEmail(emailToken);

        return idRealToken.equals(usuarioIdEnUrl);
    }

    /**
     * VISTA PARA EL MOTORIZADO
     * El motorizado consulta su propia agenda semanal.
     */
    // Cambia esto en HorarioController.java
    @GetMapping("/mi-horario/{usuarioId}") // Antes era /mi-agenda
    public ResponseEntity<List<Horario>> obtenerMiAgenda(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(horarioService.obtenerHorarioPersonal(usuarioId));
    }

    /**
     * VISTA PARA EL ADMINISTRADOR
     * Lista todos los horarios asignados en el sistema.
     */
    @GetMapping("/todos")
    public ResponseEntity<List<Horario>> listarTodo() {
        return ResponseEntity.ok(horarioService.listarTodosLosHorarios());
    }

    /**
     * ASIGNACIÓN MANUAL
     * En caso de que el admin quiera asignar a alguien específico a dedo.
     */
    @PostMapping("/asignar")
    public ResponseEntity<Horario> crearHorario(@RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.guardarHorario(horario));
    }

    /**
     * EDICIÓN DE HORAS EXTRAS
     */
    @PutMapping("/editar-horas/{id}")
    public ResponseEntity<Horario> editarHoras(
            @PathVariable UUID id,
            @RequestParam String inicio,
            @RequestParam String fin) {

        LocalTime horaInicio = LocalTime.parse(inicio);
        LocalTime horaFin = LocalTime.parse(fin);

        return ResponseEntity.ok(horarioService.actualizarHoras(id, horaInicio, horaFin));
    }
}