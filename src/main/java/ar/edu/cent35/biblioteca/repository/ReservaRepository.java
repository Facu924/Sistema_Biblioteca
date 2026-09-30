package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Reserva;
import ar.edu.cent35.biblioteca.model.enums.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByLector_IdLector(Long idLector);

    /** Cola de espera de un libro, en orden de llegada. */
    List<Reserva> findByLibro_IdLibroAndEstadoReservaOrderByFechaSolicitudAsc(Long idLibro, EstadoReserva estado);
}
