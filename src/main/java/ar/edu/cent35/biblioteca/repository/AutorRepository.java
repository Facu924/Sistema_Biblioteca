package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.inventario.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Page<Autor> findByNombreAutorContaining(String texto, Pageable pageable);

    boolean existsByNombreAutor(String nombreAutor);

    boolean existsByNombreAutorAndIdAutorNot(String nombreAutor, Long idAutor);
}
