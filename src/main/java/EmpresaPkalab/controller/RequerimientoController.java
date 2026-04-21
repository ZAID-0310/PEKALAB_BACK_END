package EmpresaPkalab.controller;

import EmpresaPkalab.model.RequerimientoTienda;
import EmpresaPkalab.service.RequerimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requerimientos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RequerimientoController {

    private final RequerimientoService requerimientoService;

    @GetMapping
    public ResponseEntity<List<RequerimientoTienda>> listar() {
        return ResponseEntity.ok(requerimientoService.listarTodo());
    }

    @PostMapping("/importar")
    public ResponseEntity<String> importarExcel(@RequestParam("file") MultipartFile file) {
        try {
            requerimientoService.importarDesdeExcel(file);
            return ResponseEntity.ok("Importación exitosa.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al importar: " + e.getMessage());
        }
    }
    @GetMapping("/mi-horario/{usuarioId}")
    public ResponseEntity<List<RequerimientoTienda>> verMiHorario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(requerimientoService.obtenerHorario(usuarioId));
    }
}