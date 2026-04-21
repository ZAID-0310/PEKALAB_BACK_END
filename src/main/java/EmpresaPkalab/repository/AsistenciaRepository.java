package EmpresaPkalab.repository;

import EmpresaPkalab.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, UUID> {

    List<Asistencia> findByUsuarioId(UUID usuarioId);

    boolean existsByRequerimientoTiendaId(UUID requerimientoTiendaId);

    Optional<Asistencia> findByUsuarioIdAndRequerimientoTiendaId(UUID usuarioId, UUID requerimientoTiendaId);

    boolean existsByUsuarioIdAndRequerimientoTiendaId(UUID usuarioId, UUID requerimientoTiendaId);

    // --- NUEVOS MÉTODOS PARA REPORTES ---

    /**
     * 1. Reporte General para el Administrador
     * Busca todas las asistencias en un rango de tiempo (ej. del lunes al domingo)
     */
    List<Asistencia> findByHoraEntradaBetweenOrderByHoraEntradaAsc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * 2. Reporte de un Motorizado Específico
     * Útil para ver el historial de un solo trabajador y calcular sus pagos
     */
    List<Asistencia> findByUsuarioIdAndHoraEntradaBetween(UUID usuarioId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * 3. Filtro de asistencias NO válidas
     * Para que el administrador vea rápidamente quiénes marcaron fuera de la tienda (Point GPS)
     */
    List<Asistencia> findByEsValidaFalseOrderByHoraEntradaDesc();
}