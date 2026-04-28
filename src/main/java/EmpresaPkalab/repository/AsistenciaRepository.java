package EmpresaPkalab.repository;

import EmpresaPkalab.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, UUID> {

    List<Asistencia> findByUsuarioId(UUID usuarioId);

    // --- CAMBIADOS: Ahora apuntan a 'horario' ---

    boolean existsByHorarioId(UUID horarioId);

    Optional<Asistencia> findByUsuarioIdAndHorarioId(UUID usuarioId, UUID horarioId);

    boolean existsByUsuarioIdAndHorarioId(UUID usuarioId, UUID horarioId);

    // --- MÉTODOS PARA REPORTES (Se mantienen igual) ---

    /**
     * 1. Reporte General para el Administrador
     */
    List<Asistencia> findByHoraEntradaBetweenOrderByHoraEntradaAsc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * 2. Reporte de un Motorizado Específico
     */
    List<Asistencia> findByUsuarioIdAndHoraEntradaBetween(UUID usuarioId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * 3. Filtro de asistencias NO válidas (Alertas)
     */
    List<Asistencia> findByEsValidaFalseOrderByHoraEntradaDesc();
}