package EmpresaPkalab.service;

import EmpresaPkalab.dto.TiendaDTO;
import EmpresaPkalab.model.Tienda;
import EmpresaPkalab.repository.TiendaRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TiendaService {

    private final TiendaRepository tiendaRepository;
    // Factoría para crear puntos geográficos (SRID 4326 es el estándar para GPS)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<Tienda> listarTodas() {
        return tiendaRepository.findAll();
    }

    public List<Tienda> buscarPorNombre(String nombre) {
        return tiendaRepository.findByNombreTiendaContainingIgnoreCase(nombre);
    }

    public Tienda registrarTienda(TiendaDTO dto) {
        Tienda tienda = new Tienda();
        mapearDtoAEntidad(tienda, dto);
        return tiendaRepository.save(tienda);
    }

    public Tienda actualizarTienda(UUID id, TiendaDTO dto) {
        Tienda tiendaExistente = tiendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));
        mapearDtoAEntidad(tiendaExistente, dto);
        return tiendaRepository.save(tiendaExistente);
    }

    private void mapearDtoAEntidad(Tienda tienda, TiendaDTO dto) {
        tienda.setNombreTienda(dto.getNombreTienda());
        tienda.setRuc(dto.getRuc());
        tienda.setDireccion(dto.getDireccion());
        tienda.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        tienda.setRadioPermitidoMetros(dto.getRadioPermitidoMetros());

        // Convertir Lat/Lng del DTO al Point de PostGIS
        if (dto.getLatitud() != 0 && dto.getLongitud() != 0) {
            // Importante: Point usa (Longitud, Latitud)
            Point punto = geometryFactory.createPoint(new Coordinate(dto.getLongitud(), dto.getLatitud()));
            tienda.setUbicacion(punto);
        }
    }

    public void eliminarTienda(UUID id) {
        tiendaRepository.deleteById(id);
    }
}
