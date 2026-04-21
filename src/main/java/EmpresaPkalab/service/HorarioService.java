package EmpresaPkalab.service;

import EmpresaPkalab.model.*;
import EmpresaPkalab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final TiendaRepository tiendaRepository;
    private final RequerimientoRepository requerimientoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * 1. ASIGNACIÓN AUTOMÁTICA SEMANAL
     * Recorre a los motorizados y les asigna la tienda más cercana con cupos disponibles.
     */
    @Transactional
    public void procesarAsignacionesSemanales(LocalDate fechaInicioSemana) {
        // LIMPIEZA: Primero borramos lo antiguo de esta semana y reseteamos cupos
        this.borrarHorariosDeLaSemana(fechaInicioSemana);
        requerimientoRepository.resetearEstadoSemanas(fechaInicioSemana);

        List<Tienda> tiendas = tiendaRepository.findAll();
        double RADIO_MAXIMO_METROS = 6000; // ~5km con SRID 4326

        for (Tienda tienda : tiendas) {
            List<RequerimientoTienda> cuposLibres = requerimientoRepository
                    .findByTiendaIdAndEstado(tienda.getId(), "PENDIENTE");

            for (RequerimientoTienda cupo : cuposLibres) {
                Usuario candidato = usuarioRepository.encontrarMotorizadoDisponible(
                        tienda.getUbicacion(),
                        cupo.getFecha(),
                        cupo.getHoraInicio(),
                        cupo.getHoraFin(),
                        RADIO_MAXIMO_METROS
                ).orElse(null);

                if (candidato != null) {
                    Horario nuevoHorario = new Horario();
                    nuevoHorario.setUsuario(candidato);
                    nuevoHorario.setTienda(tienda);

                    // --- ESTO ES LO QUE FALTABA PARA QUE EL DASHBOARD SE LLENE ---
                    nuevoHorario.setFecha(cupo.getFecha());
                    nuevoHorario.setHoraInicio(cupo.getHoraInicio());
                    nuevoHorario.setHoraFin(cupo.getHoraFin());
                    // -------------------------------------------------------------

                    nuevoHorario.setEstado("ASIGNADO");

                    horarioRepository.save(nuevoHorario);

                    cupo.setEstado("ASIGNADO");
                    requerimientoRepository.save(cupo);

                    System.out.println("ASIGNACIÓN ÓPTIMA: " + candidato.getNombre() + " -> " + tienda.getNombreTienda());
                } else {
                    System.out.println("No se encontró motorizado cerca para el cupo en " + tienda.getNombreTienda());
                }
            }
        }
    }

    /**
     * 2. GUARDA LA ASIGNACIÓN (Con validación de cruces)
     */
    public Horario guardarHorario(Horario horario) {
        boolean hayCruce = horarioRepository.existeCruceHorario(
                horario.getUsuario().getId(),
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );

        if (hayCruce) {
            throw new RuntimeException("El motorizado " + horario.getUsuario().getNombre() +
                    " ya tiene un turno que se cruza en la fecha " + horario.getFecha());
        }

        horario.setEstado("ASIGNADO");
        return horarioRepository.save(horario);
    }

    /**
     * 3. VISTA PARA EL MOTORIZADO (Horario Semanal Personal)
     */
    public List<Horario> obtenerHorarioPersonal(UUID usuarioId) {
        // Quitamos el filtro de fecha para que Android reciba datos aunque hoy no trabaje
        return horarioRepository.findByUsuarioId(usuarioId);
    }

    /**
     * 4. VISTA PARA EL ADMINISTRADOR (Tablero General)
     */
    public List<Horario> listarTodosLosHorarios() {
        // En lugar de findAll(), traemos de hoy en adelante
        return horarioRepository.findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate.now());
    }

    /**
     * 5. ACTUALIZACIÓN DE HORAS (Casos especiales o extras)
     */
    @Transactional
    public Horario actualizarHoras(UUID horarioId, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("El registro de horario no existe"));

        horario.setHoraInicio(nuevaHoraInicio);
        horario.setHoraFin(nuevaHoraFin);

        return horarioRepository.save(horario);
    }

    /**
     * 6. LIMPIEZA SEMANAL
     * Solo borra lo que se va a RE-GENERAR (futuro), manteniendo el historial (pasado).
     */
    @Transactional
    public void borrarHorariosDeLaSemana(LocalDate fechaInicio) {
        // COMENTA O ELIMINA ESTA LÍNEA:
        // horarioRepository.deleteByFechaLessThan(LocalDate.now());

        // MANTÉN ESTA: Borra solo lo que vas a sobreescribir en la nueva asignación
        horarioRepository.deleteByFechaGreaterThanEqual(fechaInicio);
    }
}