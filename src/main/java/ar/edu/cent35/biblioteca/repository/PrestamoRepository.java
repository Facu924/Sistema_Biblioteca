package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Prestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** Para el límite de préstamos simultáneos: contar con estados ACTIVO y VENCIDO. */
    long countByLector_IdLectorAndEstadoPrestamoIn(Long idLector, Collection<EstadoPrestamo> estados);

    long countByEstadoPrestamo(EstadoPrestamo estado);

    /** El préstamo abierto (ACTIVO o VENCIDO) de un ejemplar, por su número de inventario. */
    @Query("""
            select p from Prestamo p join fetch p.ejemplar e join fetch e.libro
              join fetch p.lector l join fetch l.persona
            where e.numeroInventario = :numero and p.estadoPrestamo in :estados
            """)
    Optional<Prestamo> findAbiertoPorInventario(@Param("numero") String numero,
                                                @Param("estados") Collection<EstadoPrestamo> estados);

    /** Préstamos abiertos cuya fecha límite ya pasó (mora), calculado por fecha y no por el estado guardado. */
    @Query("""
            select count(p) from Prestamo p
            where p.lector.idLector = :idLector and p.estadoPrestamo in :estados
              and p.fechaLimiteEntrega < :hoy
            """)
    long contarEnMora(@Param("idLector") Long idLector,
                      @Param("estados") Collection<EstadoPrestamo> estados,
                      @Param("hoy") LocalDate hoy);

    @Query("""
            select p from Prestamo p join fetch p.ejemplar e join fetch e.libro
              join fetch p.lector l join fetch l.persona
            where l.idLector = :idLector
            order by p.fechaPrestamo desc
            """)
    List<Prestamo> findDelLector(@Param("idLector") Long idLector);

    @Query("""
            select p from Prestamo p join fetch p.ejemplar e join fetch e.libro
              join fetch p.lector l join fetch l.persona
            where p.estadoPrestamo in :estados and p.fechaLimiteEntrega < :hoy
            order by p.fechaLimiteEntrega asc
            """)
    List<Prestamo> findEnMora(@Param("estados") Collection<EstadoPrestamo> estados,
                              @Param("hoy") LocalDate hoy);

    /**
     * Listado con filtro opcional por estado y búsqueda de texto por nombre del lector, legajo,
     * título, ISBN o número de inventario (la collation la hace insensible a acentos y mayúsculas).
     */
    @Query(value = """
            select p from Prestamo p join fetch p.ejemplar e join fetch e.libro lb
              join fetch p.lector l join fetch l.persona pe
            where (:estado is null or p.estadoPrestamo = :estado)
              and (pe.legajo like concat('%', :texto, '%')
                   or concat(pe.nombre, ' ', pe.apellido) like concat('%', :texto, '%')
                   or lb.titulo like concat('%', :texto, '%')
                   or lb.isbn like concat('%', :texto, '%')
                   or e.numeroInventario like concat('%', :texto, '%'))
            order by p.fechaPrestamo desc
            """,
            countQuery = """
            select count(p) from Prestamo p join p.ejemplar e join e.libro lb
              join p.lector l join l.persona pe
            where (:estado is null or p.estadoPrestamo = :estado)
              and (pe.legajo like concat('%', :texto, '%')
                   or concat(pe.nombre, ' ', pe.apellido) like concat('%', :texto, '%')
                   or lb.titulo like concat('%', :texto, '%')
                   or lb.isbn like concat('%', :texto, '%')
                   or e.numeroInventario like concat('%', :texto, '%'))
            """)
    Page<Prestamo> buscar(@Param("estado") EstadoPrestamo estado,
                          @Param("texto") String texto,
                          Pageable pageable);

    /** Tarea diaria: los ACTIVO con fecha límite anterior a hoy pasan a VENCIDO. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Prestamo p set p.estadoPrestamo = :nuevo
            where p.estadoPrestamo = :actual and p.fechaLimiteEntrega < :hoy
            """)
    int marcarVencidos(@Param("actual") EstadoPrestamo actual,
                       @Param("nuevo") EstadoPrestamo nuevo,
                       @Param("hoy") LocalDate hoy);
}
