package EmpresaPkalab.repository;

import EmpresaPkalab.model.RequerimientoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequerimientoRepository extends JpaRepository<RequerimientoTienda, UUID> {

    /**
     * Obtiene todos los requerimientos ordenados cronológicamente.
     * Esto ayuda a que en el Frontend no se mezclen los días ni las horas.
     */
    List<RequerimientoTienda> findAllByOrderByFechaAscHoraInicioAsc();
    List<RequerimientoTienda> findByTiendaIdAndEstado(UUID tiendaId, String estado);    /**
     * Opcional: Si en el futuro quieres filtrar solo los requerimientos
     * de una tienda específica por su nombre.
     */
    @Query("SELECT r FROM RequerimientoTienda r WHERE r.tienda.nombreTienda = :nombre")
    List<RequerimientoTienda> buscarPorNombreTienda(String nombre);
    // Quitamos el filtro de fecha para que te devuelva todos los registros de Carmen (el motorizado)
    @Query("SELECT r FROM RequerimientoTienda r WHERE r.motorizado.id = :usuarioId ORDER BY r.fecha ASC, r.horaInicio ASC")
    List<RequerimientoTienda> buscarHorarioMotorizado(@Param("usuarioId") UUID usuarioId);

    /**
     * Resetea los requerimientos a PENDIENTE a partir de una fecha específica.
     * Esto permite que el algoritmo pueda volver a asignar motorizados.
     */
    @Modifying
    @Query("UPDATE RequerimientoTienda rt SET rt.estado = 'PENDIENTE' WHERE rt.fecha >= :fechaInicio")
    void resetearEstadoSemanas(@Param("fechaInicio") LocalDate fechaInicio);
}
