package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.acceso.Lector;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectorRepository extends JpaRepository<Lector, Long> {

    Optional<Lector> findByPersona_Legajo(String legajo);

    Optional<Lector> findByPersona_IdPersona(Long idPersona);
}
