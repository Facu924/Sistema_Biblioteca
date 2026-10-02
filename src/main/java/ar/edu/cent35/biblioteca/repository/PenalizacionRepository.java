package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Penalizacion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PenalizacionRepository extends JpaRepository<Penalizacion, Long> {

    /** Vigente = activa y sin fecha de fin, o con fecha de fin de hoy en adelante. */
    @Query("""
            select p from Penalizacion p
            where p.lector.idLector = :idLector and p.activa = true
              and (p.fechaFin is null or p.fechaFin >= :hoy)
            order by p.fechaInicio desc
            """)
    List<Penalizacion> findVigentes(@Param("idLector") Long idLector, @Param("hoy") LocalDate hoy);

    @Query("""
            select p from Penalizacion p join fetch p.lector l join fetch l.persona
            where l.idLector = :idLector
            order by p.fechaInicio desc, p.idPenalizacion desc
            """)
    List<Penalizacion> findDelLector(@Param("idLector") Long idLector);

    @Query(value = """
            select p from Penalizacion p join fetch p.lector l join fetch l.persona pe
            where pe.legajo like concat('%', :legajo, '%')
              and (:soloVigentes = false
                   or (p.activa = true and (p.fechaFin is null or p.fechaFin >= :hoy)))
            order by p.fechaInicio desc, p.idPenalizacion desc
            """,
            countQuery = """
            select count(p) from Penalizacion p join p.lector l join l.persona pe
            where pe.legajo like concat('%', :legajo, '%')
              and (:soloVigentes = false
                   or (p.activa = true and (p.fechaFin is null or p.fechaFin >= :hoy)))
            """)
    Page<Penalizacion> buscar(@Param("legajo") String legajo,
                              @Param("soloVigentes") boolean soloVigentes,
                              @Param("hoy") LocalDate hoy,
                              Pageable pageable);

    /** Tarea diaria: desactiva las penalizaciones con fecha de fin ya pasada. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Penalizacion p set p.activa = false
            where p.activa = true and p.fechaFin is not null and p.fechaFin < :hoy
            """)
    int vencer(@Param("hoy") LocalDate hoy);
}
