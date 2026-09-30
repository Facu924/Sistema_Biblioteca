package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.acceso.Persona;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByLegajo(String legajo);

    boolean existsByLegajo(String legajo);
}
