package EmpresaPkalab.repository;

import EmpresaPkalab.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, UUID> {

    @Query(value = "SELECT * FROM tienda t WHERE t.estado = true ORDER BY ST_Distance(t.ubicacion, :casa) ASC", nativeQuery = true)
    List<Tienda> buscarTiendasCercanas(@Param("casa") Point casa);

    Optional<Tienda> findByNombreTienda(String nombreTienda);

    // AGREGA ESTE MÉTODO PARA QUE EL SERVICE NO DE ERROR
    Optional<Tienda> findByNombreTiendaIgnoreCase(String nombreTienda);

    List<Tienda> findByNombreTiendaContainingIgnoreCase(String nombreTienda);
}