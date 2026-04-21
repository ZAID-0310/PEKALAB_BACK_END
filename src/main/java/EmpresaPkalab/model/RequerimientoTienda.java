package EmpresaPkalab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "requerimiento_tienda", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequerimientoTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tienda_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Tienda tienda;

    @ManyToOne
    @JoinColumn(name = "motorizado_id")
    private Usuario motorizado;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // Este es el nombre que usaremos en los Repository (nMotorizado)
    @Column(name = "n_motorizado", nullable = false)
    private Integer nMotorizado;

    @Column
    private String estado = "PENDIENTE";
}