package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByMail(String mail);

    boolean existsByMail(String mail);

    boolean existsByTipoUsuario(TipoUsuario tipoUsuario);

    Optional<Usuario> findByPersona_IdPersona(Long idPersona);

    List<Usuario> findByPersona_IdPersonaIn(Collection<Long> idsPersona);

    /**
     * Listado con filtros opcionales (null = sin filtrar). El texto busca en nombre, apellido,
     * legajo y mail; la collation utf8mb4_unicode_ci lo hace insensible a acentos y mayúsculas.
     */
    @Query(value = """
            select u from Usuario u join fetch u.persona p
            where (:rol is null or u.tipoUsuario = :rol)
              and (:activo is null or u.activo = :activo)
              and (:bloqueado is null or u.cuentaBloqueada = :bloqueado)
              and (p.nombre like concat('%', :texto, '%')
                   or p.apellido like concat('%', :texto, '%')
                   or p.legajo like concat('%', :texto, '%')
                   or u.mail like concat('%', :texto, '%'))
            order by p.apellido, p.nombre
            """,
            countQuery = """
            select count(u) from Usuario u join u.persona p
            where (:rol is null or u.tipoUsuario = :rol)
              and (:activo is null or u.activo = :activo)
              and (:bloqueado is null or u.cuentaBloqueada = :bloqueado)
              and (p.nombre like concat('%', :texto, '%')
                   or p.apellido like concat('%', :texto, '%')
                   or p.legajo like concat('%', :texto, '%')
                   or u.mail like concat('%', :texto, '%'))
            """)
    Page<Usuario> buscar(@Param("texto") String texto,
                         @Param("rol") TipoUsuario rol,
                         @Param("activo") Boolean activo,
                         @Param("bloqueado") Boolean bloqueado,
                         Pageable pageable);
}
