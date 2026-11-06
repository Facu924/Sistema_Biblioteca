package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Reserva;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /** Listado general para el personal, en orden de llegada; estado null = todas. */
    @Query(value = """
            select r from Reserva r join fetch r.lector l join fetch l.persona join fetch r.libro
            where (:estado is null or r.estadoReserva = :estado)
            order by r.fechaSolicitud asc
            """,
            countQuery = """
            select count(r) from Reserva r
            where (:estado is null or r.estadoReserva = :estado)
            """)
    Page<Reserva> buscar(@Param("estado") EstadoReserva estado, Pageable pageable);

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
