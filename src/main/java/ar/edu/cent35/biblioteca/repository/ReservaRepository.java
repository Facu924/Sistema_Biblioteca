package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Reserva;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByLector_IdLectorOrderByFechaSolicitudDesc(Long idLector);

    /** Cola de espera de un libro, en orden de llegada. */
    List<Reserva> findByLibro_IdLibroAndEstadoReservaOrderByFechaSolicitudAsc(Long idLibro, EstadoReserva estado);

    boolean existsByLector_IdLectorAndLibro_IdLibroAndEstadoReserva(Long idLector, Long idLibro, EstadoReserva estado);

    Optional<Reserva> findFirstByLector_IdLectorAndLibro_IdLibroAndEstadoReservaOrderByFechaSolicitudAsc(
            Long idLector, Long idLibro, EstadoReserva estado);

    long countByLibro_IdLibroAndEstadoReserva(Long idLibro, EstadoReserva estado);

    long countByLibro_IdLibroAndEstadoReservaAndFechaSolicitudBefore(
            Long idLibro, EstadoReserva estado, LocalDateTime fecha);
}
