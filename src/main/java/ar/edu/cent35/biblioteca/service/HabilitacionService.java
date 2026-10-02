package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.circulacion.Penalizacion;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.PenalizacionRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Verifica que un lector pueda operar: usuario activo, cuenta sin bloqueo y sin penalizaciones vigentes. */
@Service
public class HabilitacionService {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;
    private final PenalizacionRepository penalizacionRepository;

    public HabilitacionService(UsuarioRepository usuarioRepository,
                               LectorRepository lectorRepository,
                               PenalizacionRepository penalizacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.lectorRepository = lectorRepository;
        this.penalizacionRepository = penalizacionRepository;
    }

    @Transactional(readOnly = true)
    public Usuario usuarioDe(Lector lector) {
        return usuarioRepository.findByPersona_IdPersona(lector.getPersona().getIdPersona())
                .orElseThrow(() -> new ReglaNegocioException("El lector no tiene un usuario asociado"));
    }

    /** El lector del usuario que tiene la sesión iniciada. */
    @Transactional(readOnly = true)
    public Lector lectorDelUsuario(String mail) {
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));
        return lectorRepository.findByPersona_IdPersona(usuario.getPersona().getIdPersona())
                .orElseThrow(() -> new ReglaNegocioException("Su usuario no tiene perfil de lector"));
    }

    @Transactional(readOnly = true)
    public void verificarPuedeOperar(Lector lector) {
        Usuario usuario = usuarioDe(lector);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario está dado de baja");
        }
        if (Boolean.TRUE.equals(usuario.getCuentaBloqueada())) {
            throw new ReglaNegocioException("La cuenta del usuario está bloqueada");
        }
        List<Penalizacion> vigentes = penalizacionRepository.findVigentes(lector.getIdLector(), LocalDate.now());
        if (!vigentes.isEmpty()) {
            Penalizacion p = vigentes.get(0);
            throw new ReglaNegocioException("El lector tiene una penalización vigente"
                    + (p.getFechaFin() == null ? " (hasta que la bibliotecaria la levante)"
                                               : " hasta el " + p.getFechaFin())
                    + ": " + p.getMotivo());
        }
    }
}
