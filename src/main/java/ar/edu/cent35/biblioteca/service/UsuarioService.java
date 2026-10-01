package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.UsuarioCreateRequest;
import ar.edu.cent35.biblioteca.dto.UsuarioResponse;
import ar.edu.cent35.biblioteca.dto.UsuarioUpdateRequest;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.enums.TipoLector;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.PersonaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de usuarios por el administrador: RF-ADM-01 a RF-ADM-04 (HU-01, HU-02). */
@Service
public class UsuarioService {

    private static final String PERFIL_DOCENTE = "DOCENTE";

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final LectorRepository lectorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
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

    // ---------- alta ----------

    /** Crea persona, usuario y (si es lector) su perfil, todo en una sola transacción. */
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest req, String actorMail, String ip) {
        String mail = normalizarMail(req.mail());
        String legajo = req.legajo().trim();

        if (usuarioRepository.existsByMail(mail)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        if (personaRepository.existsByLegajo(legajo)) {
            throw new ReglaNegocioException("Ya existe una persona con ese legajo");
        }

        PerfilLector perfil = null;
        if (req.tipoUsuario() == TipoUsuario.ROLE_LECTOR) {
            perfil = resolverPerfil(req.tipoLector(), req.perfilInstitucional());
        } else if (req.tipoLector() != null) {
            throw new ReglaNegocioException("Solo los usuarios con rol ROLE_LECTOR llevan perfil de lector");
        }

        Persona persona = new Persona();
        persona.setNombre(req.nombre().trim());
        persona.setApellido(req.apellido().trim());
        persona.setTelefono(vacioANull(req.telefono()));
        persona.setLegajo(legajo);
        persona = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setMail(mail);
        usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        usuario.setTipoUsuario(req.tipoUsuario());
        usuario = usuarioRepository.save(usuario);

        Lector lector = null;
        if (perfil != null) {
            lector = new Lector();
            lector.setPersona(persona);
            lector.setTipoLector(perfil.tipo());
            lector.setPerfilInstitucional(perfil.perfil());
            lector = lectorRepository.save(lector);
        }

        auditoriaService.registrar(actor(actorMail), "ALTA_USUARIO",
                "Alta de " + mail + " con rol " + req.tipoUsuario(), ip);
        return aRespuesta(usuario, lector);
    }

    // ---------- consulta ----------

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> buscar(String texto, TipoUsuario rol, Boolean activo,
                                                  Boolean bloqueado, int pagina, int tamanio) {
        int size = Math.min(Math.max(tamanio, 1), 100);
        Page<Usuario> resultado = usuarioRepository.buscar(
                texto == null ? "" : texto.trim(), rol, activo, bloqueado,
                PageRequest.of(Math.max(pagina, 0), size));
        List<UsuarioResponse> contenido = resultado.getContent().stream().map(this::aRespuesta).toList();
        return new PaginaResponse<>(contenido, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    // ---------- edición ----------

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest req, String actorMail, String ip) {
        Usuario usuario = buscarPorId(id);
        Persona persona = usuario.getPersona();
        boolean esElMismo = usuario.getMail().equalsIgnoreCase(actorMail);
        String mail = normalizarMail(req.mail());
        String legajo = req.legajo().trim();
        TipoUsuario nuevoRol = req.tipoUsuario();

        if (esElMismo && nuevoRol != usuario.getTipoUsuario()) {
            throw new ReglaNegocioException("No puede cambiar su propio rol");
        }
        if (esElMismo && !mail.equalsIgnoreCase(usuario.getMail())) {
            throw new ReglaNegocioException("Para cambiar su propio correo use /api/perfil/mail");
        }
        if (!mail.equalsIgnoreCase(usuario.getMail()) && usuarioRepository.existsByMail(mail)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        if (!legajo.equals(persona.getLegajo()) && personaRepository.existsByLegajo(legajo)) {
            throw new ReglaNegocioException("Ya existe una persona con ese legajo");
        }

        Lector lector = lectorRepository.findByPersona_IdPersona(persona.getIdPersona()).orElse(null);
        if (lector != null && nuevoRol != TipoUsuario.ROLE_LECTOR) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el rol de un lector con historial; dé de baja al usuario");
        }
        if (nuevoRol == TipoUsuario.ROLE_LECTOR) {
            PerfilLector perfil = resolverPerfil(req.tipoLector(), req.perfilInstitucional());
            if (lector == null) {
                lector = new Lector();
                lector.setPersona(persona);
            }
            lector.setTipoLector(perfil.tipo());
            lector.setPerfilInstitucional(perfil.perfil());
            lector = lectorRepository.save(lector);
        } else if (req.tipoLector() != null) {
            throw new ReglaNegocioException("Solo los usuarios con rol ROLE_LECTOR llevan perfil de lector");
        }

        persona.setNombre(req.nombre().trim());
        persona.setApellido(req.apellido().trim());
        persona.setTelefono(vacioANull(req.telefono()));
        persona.setLegajo(legajo);
        personaRepository.save(persona);

        usuario.setMail(mail);
        usuario.setTipoUsuario(nuevoRol);
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        }
        usuarioRepository.save(usuario);

        auditoriaService.registrar(actor(actorMail), "EDICION_USUARIO", "Edición de " + mail, ip);
        return aRespuesta(usuario, lector);
    }

    // ---------- baja y reactivación (lógicas) ----------

    @Transactional
    public UsuarioResponse darDeBaja(Long id, String actorMail, String ip) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getMail().equalsIgnoreCase(actorMail)) {
            throw new ReglaNegocioException("No puede darse de baja a sí mismo");
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(actor(actorMail), "BAJA_LOGICA_USUARIO",
                "Baja lógica de " + usuario.getMail(), ip);
        return aRespuesta(usuario);
    }

    @Transactional
    public UsuarioResponse reactivar(Long id, String actorMail, String ip) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(actor(actorMail), "REACTIVACION_USUARIO",
                "Reactivación de " + usuario.getMail(), ip);
        return aRespuesta(usuario);
    }

    // ---------- auxiliares ----------

    private record PerfilLector(TipoLector tipo, String perfil) {
    }

    private PerfilLector resolverPerfil(TipoLector tipo, String perfilIngresado) {
        if (tipo == null) {
            throw new ReglaNegocioException("El tipo de lector es obligatorio para el rol ROLE_LECTOR");
        }
        return switch (tipo) {
            case ALUMNO -> {
                if (perfilIngresado == null || perfilIngresado.isBlank()) {
                    throw new ReglaNegocioException("Para un alumno hay que indicar la carrera");
                }
                yield new PerfilLector(tipo, perfilIngresado.trim());
            }
            case PROFESOR -> new PerfilLector(tipo, PERFIL_DOCENTE);
            case PERSONAL_ADMINISTRATIVO -> new PerfilLector(tipo, null);
        };
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private Usuario actor(String mail) {
        return usuarioRepository.findByMail(mail).orElse(null);
    }

    private String normalizarMail(String mail) {
        return mail.trim().toLowerCase();
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        Lector lector = lectorRepository
                .findByPersona_IdPersona(usuario.getPersona().getIdPersona()).orElse(null);
        return aRespuesta(usuario, lector);
    }

    private UsuarioResponse aRespuesta(Usuario u, Lector l) {
        Persona p = u.getPersona();
        return new UsuarioResponse(
                u.getIdUsuario(), p.getIdPersona(), p.getNombre(), p.getApellido(),
                p.getTelefono(), p.getLegajo(), u.getMail(), u.getTipoUsuario(),
                l == null ? null : l.getTipoLector(),
                l == null ? null : l.getPerfilInstitucional(),
                u.getActivo(), u.getCuentaBloqueada(), u.getIntentosFallidos());
    }
}
