package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.inventario.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdLibroNot(String isbn, Long idLibro);

    boolean existsByAutor_IdAutor(Long idAutor);

    boolean existsByEditorial_IdEditorial(Long idEditorial);

    boolean existsByMateria_IdMateria(Long idMateria);

    /**
     * Búsqueda por título, autor, editorial o ISBN, con filtros opcionales (null = sin filtrar).
     * La collation utf8mb4_unicode_ci la hace insensible a mayúsculas y acentos (RF-BIB-02).
     */
    @Query(value = """
            select l from Libro l
              join fetch l.autor a join fetch l.editorial ed join fetch l.materia m
            where (:idAutor is null or a.idAutor = :idAutor)
              and (:idEditorial is null or ed.idEditorial = :idEditorial)
              and (:idMateria is null or m.idMateria = :idMateria)
              and (:soloDigitales is null or l.esDigital = :soloDigitales)
              and (l.titulo like concat('%', :texto, '%')
                   or a.nombreAutor like concat('%', :texto, '%')
                   or ed.nombreEditorial like concat('%', :texto, '%')
                   or l.isbn like concat('%', :texto, '%'))
            order by l.titulo
            """,
            countQuery = """
            select count(l) from Libro l
              join l.autor a join l.editorial ed join l.materia m
            where (:idAutor is null or a.idAutor = :idAutor)
              and (:idEditorial is null or ed.idEditorial = :idEditorial)
              and (:idMateria is null or m.idMateria = :idMateria)
              and (:soloDigitales is null or l.esDigital = :soloDigitales)
              and (l.titulo like concat('%', :texto, '%')
                   or a.nombreAutor like concat('%', :texto, '%')
                   or ed.nombreEditorial like concat('%', :texto, '%')
                   or l.isbn like concat('%', :texto, '%'))
            """)
    Page<Libro> buscar(@Param("texto") String texto,
                       @Param("idAutor") Long idAutor,
                       @Param("idEditorial") Long idEditorial,
                       @Param("idMateria") Long idMateria,
                       @Param("soloDigitales") Boolean soloDigitales,
                       Pageable pageable);
}
