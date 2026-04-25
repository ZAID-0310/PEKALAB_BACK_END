package EmpresaPkalab.service;

import EmpresaPkalab.dto.UsuarioDTO;
import EmpresaPkalab.model.Usuario;
import EmpresaPkalab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<Usuario> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Usuario registrarUsuario(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        mapearDtoAEntidad(usuario, dto);

        // Encriptar contraseña solo en registro
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(UUID id, UsuarioDTO dto) {
        // 1. Buscamos si el usuario existe en la base de datos
        Usuario usuarioExistente = buscarPorId(id);

        // 2. REGLA DE BLOQUEO: Si el estado es falso (inactivo), lanzamos error
        if (usuarioExistente.getEstado() == null || !usuarioExistente.getEstado()) {
            throw new RuntimeException("OPERACIÓN DENEGADA: El usuario está deshabilitado. Debe activarlo para permitir cambios.");
        }

        // 3. Si está activo, procedemos a mapear los nuevos datos del DTO
        mapearDtoAEntidad(usuarioExistente, dto);

        // 4. Manejo de contraseña: Solo se actualiza si el admin envía una nueva
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 5. Guardamos los cambios en la base de datos
        return usuarioRepository.save(usuarioExistente);
    }

    private void mapearDtoAEntidad(Usuario usuario, UsuarioDTO dto) {
        usuario.setDni(dto.getDni());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getCorreo());
        
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        if (dto.getEstado() != null) usuario.setEstado(dto.getEstado());

        // Convertir Lat/Lng del Mapa a Point de PostGIS
        if (dto.getLatitud() != 0 && dto.getLongitud() != 0) {
            Point punto = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
            usuario.setUbicacionCasa(punto);
        }
    }

    public void cambiarEstado(UUID id, Boolean estado) {
        Usuario u = buscarPorId(id);
        u.setEstado(estado);
        usuarioRepository.save(u);
    }
}