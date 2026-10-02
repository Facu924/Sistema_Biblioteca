package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.PenalizacionRequest;
import ar.edu.cent35.biblioteca.dto.PenalizacionResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.circulacion.HistorialTransaccion;
import ar.edu.cent35.biblioteca.model.circulacion.Penalizacion;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.PenalizacionRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sanciones: las genera el sistema al devolver, o las carga la bibliotecaria; ella decide cómo se tratan. */
@Service
public class PenalizacionService {

    private final PenalizacionRepository penalizacionRepository;
    private final LectorRepository lectorRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilitacionService habilitacionService;
    private final AuditoriaService auditoriaService;

    public PenalizacionService(PenalizacionRepository penalizacionRepository,
                               LectorRepository lectorRepository,
                               UsuarioRepository usuarioRepository,
                               HabilitacionService habilitacionService,
                               AuditoriaService auditoriaService) {
        this.penalizacionRepository = penalizacionRepository;
        this.lectorRepository = lectorRepository;
        this.usuarioRepository = usuarioRepository;
        this.habilitacionService = habilitacionService;
        this.auditoriaService = auditoriaService;
    }

    /** La usa DevolucionService; fechaFin null = hasta que se levante. */
    @Transactional
    public Penalizacion crear(Lector lector, HistorialTransaccion historial, String motivo,
                              LocalDate inicio, LocalDate fin, Usuario actor, String ip) {
        Penalizacion p = new Penalizacion();
        p.setLector(lector);
        p.setHistorial(historial);
        p.setMotivo(recortar(motivo, 300));
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setActiva(true);
        p = penalizacionRepository.save(p);
        auditoriaService.registrar(actor, "PENALIZACION_CREADA",
                "Lector " + lector.getPersona().getLegajo() + ": " + p.getMotivo()
                        + (fin == null ? " (sin fecha de fin)" : " (hasta " + fin + ")"), ip);
        return p;
    }

    @Transactional
    public PenalizacionResponse crearManual(PenalizacionRequest req, String actorMail, String ip) {
        Lector lector = lectorRepository.findByPersona_Legajo(req.legajo().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un lector con ese legajo"));
        LocalDate hoy = LocalDate.now();
        LocalDate fin = req.dias() == null ? null : hoy.plusDays(req.dias());
        Usuario actor = usuarioRepository.findByMail(actorMail).orElse(null);
        return aRespuesta(crear(lector, null, req.motivo().trim(), hoy, fin, actor, ip));
    }

    @Transactional
    public PenalizacionResponse levantar(Long id, String actorMail, String ip) {
        Penalizacion p = penalizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penalización no encontrada: " + id));
        if (!Boolean.TRUE.equals(p.getActiva())) {
            throw new ReglaNegocioException("La penalización ya no está activa");
        }
        LocalDate hoy = LocalDate.now();
        p.setActiva(false);
        if (p.getFechaFin() == null || p.getFechaFin().isAfter(hoy)) {
            p.setFechaFin(hoy); // queda registrado cuándo se levantó
        }
        penalizacionRepository.save(p);
        auditoriaService.registrar(usuarioRepository.findByMail(actorMail).orElse(null),
                "PENALIZACION_LEVANTADA",
                "Se levantó la penalización " + id + " del lector " + p.getLector().getPersona().getLegajo(), ip);
        return aRespuesta(p);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PenalizacionResponse> listar(String legajo, boolean soloVigentes, int pagina, int tamanio) {
        Page<Penalizacion> r = penalizacionRepository.buscar(
                legajo == null ? "" : legajo.trim(), soloVigentes, LocalDate.now(),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100)));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<PenalizacionResponse> mias(String mail) {
        Lector lector = habilitacionService.lectorDelUsuario(mail);
        return penalizacionRepository.findDelLector(lector.getIdLector())
                .stream().map(this::aRespuesta).toList();
    }

    public PenalizacionResponse aRespuesta(Penalizacion p) {
        Persona persona = p.getLector().getPersona();
        boolean vigente = Boolean.TRUE.equals(p.getActiva())
                && (p.getFechaFin() == null || !p.getFechaFin().isBefore(LocalDate.now()));
        return new PenalizacionResponse(p.getIdPenalizacion(), p.getLector().getIdLector(),
                persona.getLegajo(), persona.getNombre() + " " + persona.getApellido(),
                p.getMotivo(), p.getFechaInicio(), p.getFechaFin(), p.getActiva(), vigente);
    }

    private String recortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
