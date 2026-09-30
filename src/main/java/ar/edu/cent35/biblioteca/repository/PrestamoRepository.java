package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Prestamo;
import ar.edu.cent35.biblioteca.model.enums.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByLector_IdLector(Long idLector);

    /** Para el límite de 2 préstamos simultáneos: contar con estados ACTIVO y VENCIDO. */
    long countByLector_IdLectorAndEstadoPrestamoIn(Long idLector, Collection<EstadoPrestamo> estados);

    Optional<Prestamo> findByEjemplar_IdEjemplarAndEstadoPrestamoIn(Long idEjemplar, Collection<EstadoPrestamo> estados);

    /** Para marcar préstamos vencidos. */
    List<Prestamo> findByEstadoPrestamoAndFechaLimiteEntregaBefore(EstadoPrestamo estado, LocalDate fecha);
}
