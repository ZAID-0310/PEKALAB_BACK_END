package EmpresaPkalab.controller;

import EmpresaPkalab.dto.UsuarioDTO;
import EmpresaPkalab.model.Usuario;
import EmpresaPkalab.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    // BUSCADOR POR NOMBRE: Ideal para filtrar la lista en el frontend
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuario>> buscarPorNombre(@RequestParam String nombre) {
        List<Usuario> resultados = usuarioService.buscarPorNombre(nombre);
        return ResponseEntity.ok(resultados);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrar(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.registrarUsuario(usuarioDTO));
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> editar(@PathVariable UUID id, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO));
    }

    // BUSCADOR POR DNI: Para que el Admin encuentre al motorizado al toque
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Usuario> buscarPorDni(@PathVariable String dni) {
        return usuarioService.buscarPorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable UUID id, @RequestParam Boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.ok("Estado actualizado");
    }


}