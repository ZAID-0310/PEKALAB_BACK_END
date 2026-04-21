package EmpresaPkalab.dto;

import lombok.Data;
import java.util.UUID; // <--- Importante

@Data
public class MarcadoRequest {
    private UUID usuarioId;      // El usuario sigue siendo Long
    private UUID requerimientoId; // CAMBIA esto de Long a UUID
    private Double latitud;
    private Double longitud;
}