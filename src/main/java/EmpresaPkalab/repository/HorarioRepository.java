package EmpresaPkalab.repository;

import EmpresaPkalab.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, UUID> {

    // 1. Para que el motorizado vea su agenda completa
    List<Horario> findByUsuarioId(UUID usuarioId);

    // 2. Para la vista semanal del motorizado
    List<Horario> findByUsuarioIdAndFechaGreaterThanEqualOrderByFechaAsc(UUID usuarioId, LocalDate fecha);

    // 3. Para que el administrador vea quiénes trabajan hoy
    List<Horario> findByFecha(LocalDate fecha);

    // --- MÉTODOS PARA EL DASHBOARD REINICIADO (ADMIN) ---
    List<Horario> findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha);

    // --- NUEVO: MÉTODO PARA REPORTES HISTÓRICOS ---
    // Esto te permitirá buscar, por ejemplo, todo lo de marzo 2026
    List<Horario> findByFechaBetweenOrderByFechaAsc(LocalDate inicio, LocalDate fin);

    // --- MÉTODOS DE LIMPIEZA ---
    @Transactional
    @Modifying
    void deleteByFechaGreaterThanEqual(LocalDate fecha);

    @Transactional
    @Modifying
    @Query("DELETE FROM Horario h WHERE h.fecha < :fecha")
    void deleteByFechaLessThan(@Param("fecha") LocalDate fecha);

    /**
     * Verifica cruces de horario para evitar doble asignación
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h " +
            "WHERE h.usuario.id = :usuarioId " +
            "AND h.fecha = :fecha " +
            "AND (:inicio < h.horaFin AND :fin > h.horaInicio)")
    boolean existeCruceHorario(@Param("usuarioId") UUID usuarioId,
                               @Param("fecha") LocalDate fecha,
                               @Param("inicio") LocalTime inicio,
                               @Param("fin") LocalTime fin);
}