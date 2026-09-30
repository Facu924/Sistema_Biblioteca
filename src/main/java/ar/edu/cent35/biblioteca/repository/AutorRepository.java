package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.inventario.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

}
