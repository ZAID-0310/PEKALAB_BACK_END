package EmpresaPkalab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asistencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    //relacion con la tabla requerimientoExcel (cupo)
    /*@ManyToOne
    @JoinColumn(name = "requerimiento_id", nullable = false)
    private RequerimientoTienda requerimientoTienda;
*/
    //Relacion con el Motorizado
    @ManyToOne
    @JoinColumn(name = "usuario_id",nullable = false)
    private Usuario usuario;

    // En Asistencia.java
    @ManyToOne
    @JoinColumn(name = "horario_id", nullable = false) // Cambiamos el nombre de la columna
    private Horario horario; // Ahora apunta a Horario

    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;

    @Column(columnDefinition = "geography(Point, 4326)")
    private  Point ubicacionMarcado;

    // Para saber si marcó dentro del rango de la tienda
    private Boolean esValida = false;

    // Podemos agregar un campo de observación (ej. "Llegó tarde")
    private String observacion;


}