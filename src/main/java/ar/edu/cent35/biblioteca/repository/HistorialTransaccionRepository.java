package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.HistorialTransaccion;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistorialTransaccionRepository extends JpaRepository<HistorialTransaccion, Long> {

    List<HistorialTransaccion> findByLector_IdLector(Long idLector);

    List<HistorialTransaccion> findByEjemplar_IdEjemplar(Long idEjemplar);

    @Query(value = """
            select h from HistorialTransaccion h
              join fetch h.ejemplar e join fetch e.libro
              join fetch h.lector l join fetch l.persona pe
            where pe.legajo like concat('%', :legajo, '%')
              and e.numeroInventario like concat('%', :inventario, '%')
            order by h.fechaDevolucionReal desc
            """,
            countQuery = """
            select count(h) from HistorialTransaccion h
              join h.ejemplar e join h.lector l join l.persona pe
            where pe.legajo like concat('%', :legajo, '%')
              and e.numeroInventario like concat('%', :inventario, '%')
            """)
    Page<HistorialTransaccion> buscar(@Param("legajo") String legajo,
                                      @Param("inventario") String inventario,
                                      Pageable pageable);
}
