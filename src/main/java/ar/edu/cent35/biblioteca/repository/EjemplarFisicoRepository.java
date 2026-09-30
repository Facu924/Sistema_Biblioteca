package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.enums.*;

import java.util.List;
import java.util.Optional;

import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjemplarFisicoRepository extends JpaRepository<EjemplarFisico, Long> {

    Optional<EjemplarFisico> findByNumeroInventario(String numeroInventario);

    List<EjemplarFisico> findByLibro_IdLibro(Long idLibro);

    /** Sirve para la regla de stock mínimo (contar ejemplares DISPONIBLE de un libro). */
    long countByLibro_IdLibroAndEstadoEjemplar(Long idLibro, EstadoEjemplar estado);
}
