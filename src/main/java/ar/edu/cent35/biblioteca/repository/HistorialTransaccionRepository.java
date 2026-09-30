package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.HistorialTransaccion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialTransaccionRepository extends JpaRepository<HistorialTransaccion, Long> {

    List<HistorialTransaccion> findByLector_IdLector(Long idLector);

    List<HistorialTransaccion> findByEjemplar_IdEjemplar(Long idEjemplar);
}
