package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EjemplarFisicoRepository extends JpaRepository<EjemplarFisico, Long> {

    Optional<EjemplarFisico> findByNumeroInventario(String numeroInventario);

    boolean existsByNumeroInventario(String numeroInventario);

    boolean existsByNumeroInventarioAndIdEjemplarNot(String numeroInventario, Long idEjemplar);

    boolean existsByLibro_IdLibro(Long idLibro);

    List<EjemplarFisico> findByLibro_IdLibro(Long idLibro);

    List<EjemplarFisico> findByLibro_IdLibroOrderByNumeroInventario(Long idLibro);

    /** Sirve para la regla de stock mínimo (contar ejemplares DISPONIBLE de un libro). */
    long countByLibro_IdLibroAndEstadoEjemplar(Long idLibro, EstadoEjemplar estado);

    /** Cantidades agrupadas por libro y estado, en una sola consulta: [idLibro, estado, cantidad]. */
    @Query("""
            select e.libro.idLibro, e.estadoEjemplar, count(e)
            from EjemplarFisico e
            where e.libro.idLibro in :ids
            group by e.libro.idLibro, e.estadoEjemplar
            """)
    List<Object[]> contarPorLibroYEstado(@Param("ids") Collection<Long> ids);
}
