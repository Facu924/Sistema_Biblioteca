package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.LibroRequest;
import ar.edu.cent35.biblioteca.dto.LibroResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.inventario.Libro;
import ar.edu.cent35.biblioteca.repository.AutorRepository;
import ar.edu.cent35.biblioteca.repository.EditorialRepository;
import ar.edu.cent35.biblioteca.repository.EjemplarFisicoRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import ar.edu.cent35.biblioteca.repository.MateriaCduRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Catálogo de libros: RF-BIB-01 y RF-BIB-02, búsqueda y material digital (RF-ALU-01). */
@Service
public class LibroService {

    private static final MediaType EPUB = MediaType.parseMediaType("application/epub+zip");

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final EditorialRepository editorialRepository;
    private final MateriaCduRepository materiaRepository;
    private final EjemplarFisicoRepository ejemplarRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ArchivoService archivoService;

    public LibroService(LibroRepository libroRepository,
                        AutorRepository autorRepository,
                        EditorialRepository editorialRepository,
                        MateriaCduRepository materiaRepository,
                        EjemplarFisicoRepository ejemplarRepository,
                        UsuarioRepository usuarioRepository,
                        AuditoriaService auditoriaService,
                        ArchivoService archivoService) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.editorialRepository = editorialRepository;
        this.materiaRepository = materiaRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.archivoService = archivoService;
    }

    // ---------- consulta ----------

    @Transactional(readOnly = true)
    public PaginaResponse<LibroResponse> buscar(String texto, Long idAutor, Long idEditorial, Long idMateria,
                                                Boolean soloDigitales, int pagina, int tamanio) {
        Page<Libro> r = libroRepository.buscar(
                texto == null ? "" : texto.trim(), idAutor, idEditorial, idMateria,
                Boolean.TRUE.equals(soloDigitales) ? Boolean.TRUE : null,
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100)));
        Map<Long, long[]> stock = stock(r.getContent().stream().map(Libro::getIdLibro).toList());
        List<LibroResponse> contenido = r.getContent().stream()
                .map(l -> aRespuesta(l, stock.get(l.getIdLibro()))).toList();
        return new PaginaResponse<>(contenido, r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return respuesta(buscarPorId(id));
    }

    // ---------- alta, edición y baja ----------

    @Transactional
    public LibroResponse crear(LibroRequest req) {
        String isbn = normalizarIsbn(req.isbn());
        if (isbn != null && libroRepository.existsByIsbn(isbn)) {
            throw new ReglaNegocioException("Ya existe un libro con ese ISBN");
        }
        Libro libro = new Libro();
        aplicar(libro, req, isbn);
        libro.setEsDigital(false);
        return respuesta(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest req) {
        Libro libro = buscarPorId(id);
        String isbn = normalizarIsbn(req.isbn());
        if (isbn != null && libroRepository.existsByIsbnAndIdLibroNot(isbn, id)) {
            throw new ReglaNegocioException("Ya existe otro libro con ese ISBN");
        }
        aplicar(libro, req, isbn);
        return respuesta(libroRepository.save(libro));
    }

    @Transactional
    public void eliminar(Long id, String actorMail, String ip) {
        Libro libro = buscarPorId(id);
        if (ejemplarRepository.existsByLibro_IdLibro(id)) {
            throw new ReglaNegocioException(
                    "El libro tiene ejemplares registrados y no puede eliminarse; dé de baja los ejemplares");
        }
        String archivo = libro.getRutaArchivoDigital();
        String titulo = libro.getTitulo();
        libroRepository.delete(libro);
        libroRepository.flush(); // si hay reservas u otras relaciones, falla acá y no se borra el archivo
        if (archivo != null) {
            archivoService.eliminar(archivo);
        }
        auditoriaService.registrar(usuarioRepository.findByMail(actorMail).orElse(null),
                "BAJA_LIBRO", "Baja del libro \"" + titulo + "\" (id " + id + ")", ip);
    }

    // ---------- material digital ----------

    @Transactional
    public LibroResponse subirArchivo(Long id, MultipartFile archivo) {
        Libro libro = buscarPorId(id);
        String anterior = libro.getRutaArchivoDigital();
        String nuevo = archivoService.guardar(id, archivo);
        libro.setRutaArchivoDigital(nuevo);
        libro.setEsDigital(true);
        libroRepository.save(libro);
        if (anterior != null) {
            archivoService.eliminar(anterior);
        }
        return respuesta(libro);
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga obtenerArchivo(Long id) {
        Libro libro = buscarPorId(id);
        String ruta = libro.getRutaArchivoDigital();
        if (ruta == null || ruta.isBlank()) {
            throw new ResourceNotFoundException("El libro no tiene archivo digital");
        }
        boolean epub = ruta.endsWith(".epub");
        return new ArchivoDescarga(archivoService.cargar(ruta),
                "libro-" + id + (epub ? ".epub" : ".pdf"),
                epub ? EPUB : MediaType.APPLICATION_PDF);
    }

    @Transactional
    public LibroResponse quitarArchivo(Long id) {
        Libro libro = buscarPorId(id);
        String ruta = libro.getRutaArchivoDigital();
        if (ruta == null || ruta.isBlank()) {
            throw new ResourceNotFoundException("El libro no tiene archivo digital");
        }
        libro.setRutaArchivoDigital(null);
        libro.setEsDigital(false);
        libroRepository.save(libro);
        archivoService.eliminar(ruta);
        return respuesta(libro);
    }

    // ---------- auxiliares ----------

    private void aplicar(Libro libro, LibroRequest req, String isbn) {
        libro.setIsbn(isbn);
        libro.setTitulo(req.titulo().trim());
        libro.setAutor(autorRepository.findById(req.idAutor())
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado: " + req.idAutor())));
        libro.setEditorial(editorialRepository.findById(req.idEditorial())
                .orElseThrow(() -> new ResourceNotFoundException("Editorial no encontrada: " + req.idEditorial())));
        libro.setMateria(materiaRepository.findById(req.idMateria())
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + req.idMateria())));
        libro.setAnioPublicacion(req.anioPublicacion());
        libro.setNumeroPaginas(req.numeroPaginas());
        libro.setEdicion(vacioANull(req.edicion()));
        libro.setResumen(vacioANull(req.resumen()));
    }

    /** Quita guiones y espacios; devuelve null si no hay ISBN. No verifica el dígito de control. */
    private String normalizarIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }
        String limpio = isbn.replaceAll("[\\s-]", "").toUpperCase();
        if (!limpio.matches("\\d{9}[\\dX]|\\d{13}")) {
            throw new ReglaNegocioException("ISBN inválido: debe tener 10 o 13 dígitos");
        }
        return limpio;
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + id));
    }

    /** Devuelve, por libro, [total sin bajas, disponibles] con una sola consulta. */
    private Map<Long, long[]> stock(Collection<Long> ids) {
        Map<Long, long[]> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        for (Object[] fila : ejemplarRepository.contarPorLibroYEstado(ids)) {
            Long idLibro = (Long) fila[0];
            EstadoEjemplar estado = (EstadoEjemplar) fila[1];
            long cantidad = (Long) fila[2];
            long[] valores = mapa.computeIfAbsent(idLibro, k -> new long[2]);
            if (estado != EstadoEjemplar.BAJA_LOGICA) {
                valores[0] += cantidad;
            }
            if (estado == EstadoEjemplar.DISPONIBLE) {
                valores[1] += cantidad;
            }
        }
        return mapa;
    }

    private LibroResponse respuesta(Libro libro) {
        return aRespuesta(libro, stock(List.of(libro.getIdLibro())).get(libro.getIdLibro()));
    }

    private LibroResponse aRespuesta(Libro l, long[] st) {
        long total = st == null ? 0 : st[0];
        long disponibles = st == null ? 0 : st[1];
        return new LibroResponse(
                l.getIdLibro(), l.getIsbn(), l.getTitulo(),
                l.getAutor().getIdAutor(), l.getAutor().getNombreAutor(),
                l.getEditorial().getIdEditorial(), l.getEditorial().getNombreEditorial(),
                l.getMateria().getIdMateria(), l.getMateria().getCodigoCdu(), l.getMateria().getNombreTema(),
                l.getAnioPublicacion(), l.getNumeroPaginas(), l.getEdicion(), l.getResumen(),
                l.getEsDigital(), total, disponibles, disponibles > 1);
    }
}
