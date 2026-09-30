package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.acceso.Usuario;

import java.util.Optional;

import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByTipoUsuario(TipoUsuario tipoUsuario);

    Optional<Usuario> findByMail(String mail);

    boolean existsByMail(String mail);
}
