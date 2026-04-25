package EmpresaPkalab.repository;

import EmpresaPkalab.model.Usuario;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByDni(String dni);

    List<Usuario> findByRolAndEstado(String rol, Boolean estado);

    //busqueda por nombre

    List<Usuario> findByNombreContainingIgnoreCase(String nombre);


    /**
     * Busca al motorizado más cercano disponible que no tenga cruces de horario.
     * Utiliza el operador <-> de PostGIS para una búsqueda eficiente por índice GIST.
     */
    @Query(value = """
    SELECT u.* FROM usuario u
    WHERE u.rol = 'MOTORIZADO'
    AND u.estado = true
    AND (:radioMaximo IS NULL OR (u.ubicacion_casa <-> :ubicacionTienda) <= :radioMaximo)
    AND NOT EXISTS (
        SELECT 1 FROM horario h
        WHERE h.usuario_id = u.id
        AND h.fecha = :fecha
        AND (h.hora_inicio < :horaFin AND h.hora_fin > :horaInicio)
    )
    ORDER BY u.ubicacion_casa <-> :ubicacionTienda ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Usuario> encontrarMotorizadoDisponible(
            @Param("ubicacionTienda") Point ubicacionTienda,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("radioMaximo") Double radioMaximo
    );
}