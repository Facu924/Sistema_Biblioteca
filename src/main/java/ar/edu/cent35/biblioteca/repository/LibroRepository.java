package ar.edu.cent35.biblioteca.repository;

import java.util.List;
import java.util.Optional;

import ar.edu.cent35.biblioteca.model.inventario.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    /** Búsqueda por título o autor; la collation utf8mb4_unicode_ci la vuelve insensible a acentos y mayúsculas. */
    List<Libro> findByTituloContainingOrAutor_NombreAutorContaining(String titulo, String autor);

    Optional<Libro> findByIsbn(String isbn);
}
