package EmpresaPkalab.model;

import org.locationtech.jts.geom.Point; // ESTE ES EL CORRECTO PARA SPRING BOOT 3
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(unique = true, nullable = false, length = 150)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 50)
    private String rol; // ADMIN o MOTORIZADO

    @Column
    private Boolean estado = true;

    @Column(name = "ubicacion_casa", columnDefinition = "geography(Point, 4326)")
    private Point ubicacionCasa;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // NOTA: No hace falta escribir getDni, setPassword, etc.
    // La anotación @Data de arriba los crea automáticamente en memoria.
}