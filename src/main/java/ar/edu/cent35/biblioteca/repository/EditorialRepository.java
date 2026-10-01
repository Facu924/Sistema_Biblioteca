package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.inventario.Editorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditorialRepository extends JpaRepository<Editorial, Long> {

    Page<Editorial> findByNombreEditorialContaining(String texto, Pageable pageable);

    boolean existsByNombreEditorial(String nombreEditorial);

    boolean existsByNombreEditorialAndIdEditorialNot(String nombreEditorial, Long idEditorial);
}
