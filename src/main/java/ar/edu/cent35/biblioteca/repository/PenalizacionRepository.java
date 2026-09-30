package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.circulacion.Penalizacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PenalizacionRepository extends JpaRepository<Penalizacion, Long> {

    /** Para validar que el lector no tenga sanciones vigentes. */
    boolean existsByLector_IdLectorAndActivaTrue(Long idLector);

    List<Penalizacion> findByLector_IdLector(Long idLector);
}
