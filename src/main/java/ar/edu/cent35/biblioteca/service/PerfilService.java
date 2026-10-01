package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.UsuarioResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.PersonaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autogestión del propio perfil, para cualquier usuario autenticado (RF-GEN-01). */
@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final LectorRepository lectorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public PerfilService(UsuarioRepository usuarioRepository,
                         PersonaRepository personaRepository,
                         LectorRepository lectorRepository,
                         PasswordEncoder passwordEncoder,
                         AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.lectorRepository = lectorRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(String mail) {
        Usuario u = propio(mail);
        return aRespuesta(u);
    }

    @Transactional
    public UsuarioResponse actualizarTelefono(String mail, String telefono) {
        Usuario u = propio(mail);
        Persona p = u.getPersona();
        p.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        personaRepository.save(p);
        return aRespuesta(u);
    }

    /** Exige la contraseña actual. Después de cambiar el correo hay que volver a iniciar sesión. */
    @Transactional
    public void cambiarMail(String mailActual, String contrasenaActual, String nuevoMail, String ip) {
        Usuario u = propio(mailActual);
        verificarContrasena(u, contrasenaActual);
        String nuevo = nuevoMail.trim().toLowerCase();
        if (!nuevo.equalsIgnoreCase(u.getMail()) && usuarioRepository.existsByMail(nuevo)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        u.setMail(nuevo);
        usuarioRepository.save(u);
        auditoriaService.registrar(u, "CAMBIO_MAIL", "Cambio de correo: " + mailActual + " a " + nuevo, ip);
    }

    @Transactional
    public void cambiarContrasena(String mail, String contrasenaActual, String contrasenaNueva, String ip) {
        Usuario u = propio(mail);
        verificarContrasena(u, contrasenaActual);
        u.setContrasena(passwordEncoder.encode(contrasenaNueva));
        usuarioRepository.save(u);
        auditoriaService.registrar(u, "CAMBIO_CONTRASENA", "Cambio de contraseña de " + mail, ip);
    }

    private void verificarContrasena(Usuario u, String contrasenaActual) {
        if (!passwordEncoder.matches(contrasenaActual, u.getContrasena())) {
            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }
    }

    private Usuario propio(String mail) {
        return usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        Persona p = u.getPersona();
        Lector l = lectorRepository.findByPersona_IdPersona(p.getIdPersona()).orElse(null);
        return new UsuarioResponse(
                u.getIdUsuario(), p.getIdPersona(), p.getNombre(), p.getApellido(),
                p.getTelefono(), p.getLegajo(), u.getMail(), u.getTipoUsuario(),
                l == null ? null : l.getTipoLector(),
                l == null ? null : l.getPerfilInstitucional(),
                u.getActivo(), u.getCuentaBloqueada(), u.getIntentosFallidos());
    }
}
