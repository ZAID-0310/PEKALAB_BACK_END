package EmpresaPkalab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AsistenciaAdminDTO {
    private UUID id;
    private String nombreMotorizado;
    private String nombreTienda;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private boolean esValida;
    private String observacion;
}
