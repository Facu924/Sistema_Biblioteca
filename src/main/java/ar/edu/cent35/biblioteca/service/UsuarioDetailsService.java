package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga el usuario por mail. Informa si la cuenta está bloqueada o inactiva,
 * y Spring Security rechaza el ingreso antes de comparar la contraseña.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getMail())
                .password(usuario.getContrasena())
                .authorities(usuario.getTipoUsuario().name())
                .accountLocked(Boolean.TRUE.equals(usuario.getCuentaBloqueada()))
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
