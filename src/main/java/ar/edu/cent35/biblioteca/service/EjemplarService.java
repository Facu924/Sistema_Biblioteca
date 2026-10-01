package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.CambioEstadoRequest;
import ar.edu.cent35.biblioteca.dto.EjemplarRequest;
import ar.edu.cent35.biblioteca.dto.EjemplarResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import ar.edu.cent35.biblioteca.model.inventario.Libro;
import ar.edu.cent35.biblioteca.repository.EjemplarFisicoRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Copias físicas: alta, búsqueda por número de inventario (código de barras) y cambios de estado. */
@Service
public class EjemplarService {

    private final EjemplarFisicoRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public EjemplarService(EjemplarFisicoRepository ejemplarRepository,
                           LibroRepository libroRepository,
                           UsuarioRepository usuarioRepository,
                           AuditoriaService auditoriaService) {
        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public EjemplarResponse crear(Long idLibro, EjemplarRequest req) {
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + idLibro));
        String inventario = req.numeroInventario().trim();
        if (ejemplarRepository.existsByNumeroInventario(inventario)) {
            throw new ReglaNegocioException("Ya existe un ejemplar con ese número de inventario");
        }
        EjemplarFisico e = new EjemplarFisico();
        e.setLibro(libro);
        e.setNumeroInventario(inventario);
        e.setUbicacionEstante(vacioANull(req.ubicacionEstante()));
        e.setEstadoEjemplar(EstadoEjemplar.DISPONIBLE);
        return aRespuesta(ejemplarRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<EjemplarResponse> listarPorLibro(Long idLibro) {
        if (!libroRepository.existsById(idLibro)) {
            throw new ResourceNotFoundException("Libro no encontrado: " + idLibro);
        }
        return ejemplarRepository.findByLibro_IdLibroOrderByNumeroInventario(idLibro)
                .stream().map(this::aRespuesta).toList();
    }

    /** Búsqueda por el número que lee el lector de códigos de barras. */
    @Transactional(readOnly = true)
    public EjemplarResponse buscarPorInventario(String numero) {
        return aRespuesta(ejemplarRepository.findByNumeroInventario(numero.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ejemplar con inventario " + numero)));
    }

    @Transactional
    public EjemplarResponse actualizar(Long id, EjemplarRequest req) {
        EjemplarFisico e = buscar(id);
        String inventario = req.numeroInventario().trim();
        if (ejemplarRepository.existsByNumeroInventarioAndIdEjemplarNot(inventario, id)) {
            throw new ReglaNegocioException("Ya existe otro ejemplar con ese número de inventario");
        }
        e.setNumeroInventario(inventario);
        e.setUbicacionEstante(vacioANull(req.ubicacionEstante()));
        return aRespuesta(ejemplarRepository.save(e));
    }

    /**
     * Cambio manual de estado: DISPONIBLE, DANADO, EXTRAVIADO o BAJA_LOGICA (expurgo).
     * PRESTADO lo maneja el sistema con los préstamos y devoluciones.
     */
    @Transactional
    public EjemplarResponse cambiarEstado(Long id, CambioEstadoRequest req, String actorMail, String ip) {
        EjemplarFisico e = buscar(id);
        EstadoEjemplar anterior = e.getEstadoEjemplar();
        EstadoEjemplar nuevo = req.estado();
        String observaciones = vacioANull(req.observaciones());

        if (nuevo == EstadoEjemplar.PRESTADO) {
            throw new ReglaNegocioException("El estado PRESTADO lo asigna el sistema al registrar un préstamo");
        }
        if (anterior == EstadoEjemplar.PRESTADO) {
            throw new ReglaNegocioException("El ejemplar está prestado; registre primero la devolución");
        }
        if (anterior == nuevo) {
            throw new ReglaNegocioException("El ejemplar ya está en estado " + nuevo);
        }
        if (nuevo != EstadoEjemplar.DISPONIBLE && observaciones == null) {
            throw new ReglaNegocioException("Indique el motivo en las observaciones");
        }

        e.setEstadoEjemplar(nuevo);
        ejemplarRepository.save(e);
        auditoriaService.registrar(usuarioRepository.findByMail(actorMail).orElse(null),
                "CAMBIO_ESTADO_EJEMPLAR",
                "Ejemplar " + e.getNumeroInventario() + ": " + anterior + " a " + nuevo
                        + (observaciones == null ? "" : ". " + observaciones),
                ip);
        return aRespuesta(e);
    }

    private EjemplarFisico buscar(Long id) {
        return ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado: " + id));
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private EjemplarResponse aRespuesta(EjemplarFisico e) {
        return new EjemplarResponse(e.getIdEjemplar(), e.getLibro().getIdLibro(), e.getLibro().getTitulo(),
                e.getNumeroInventario(), e.getUbicacionEstante(), e.getEstadoEjemplar());
    }
}
