package EmpresaPkalab.controller;

import EmpresaPkalab.dto.LoginRequest;
import EmpresaPkalab.model.Usuario;
import EmpresaPkalab.service.AuthService;
import EmpresaPkalab.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = authService.login(request.getCorreo(), request.getPassword());
            String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());

            // AGREGAMOS EL "id" AL MAPA DE RESPUESTA
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "rol", usuario.getRol(),
                    "nombre", usuario.getNombre(),
                    "id", usuario.getId() // <--- Asegúrate que tu modelo Usuario tenga getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}