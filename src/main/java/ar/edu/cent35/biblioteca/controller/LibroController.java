package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.EjemplarRequest;
import ar.edu.cent35.biblioteca.dto.EjemplarResponse;
import ar.edu.cent35.biblioteca.dto.LibroRequest;
import ar.edu.cent35.biblioteca.dto.LibroResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.service.ArchivoDescarga;
import ar.edu.cent35.biblioteca.service.EjemplarService;
import ar.edu.cent35.biblioteca.service.LibroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Consulta del catálogo: cualquier usuario autenticado (RF-ALU-01).
 * Altas, bajas y modificaciones: solo la bibliotecaria (RF-BIB-01). El administrador solo lee (RF-ADM-06).
 */
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";
    private static final String PERSONAL = "hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')";

    private final LibroService libroService;
    private final EjemplarService ejemplarService;

    public LibroController(LibroService libroService, EjemplarService ejemplarService) {
        this.libroService = libroService;
        this.ejemplarService = ejemplarService;
    }

    @GetMapping
    public PaginaResponse<LibroResponse> buscar(@RequestParam(required = false) String texto,
                                                @RequestParam(required = false) Long idAutor,
                                                @RequestParam(required = false) Long idEditorial,
                                                @RequestParam(required = false) Long idMateria,
                                                @RequestParam(required = false) Boolean soloDigitales,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "20") int tamanio) {
        return libroService.buscar(texto, idAutor, idEditorial, idMateria, soloDigitales, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public LibroResponse crear(@Valid @RequestBody LibroRequest req) {
        return libroService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(BIBLIOTECARIA)
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest req) {
        return libroService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(BIBLIOTECARIA)
    public void eliminar(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        libroService.eliminar(id, auth.getName(), request.getRemoteAddr());
    }

    // ---------- ejemplares de un libro ----------

    @GetMapping("/{id}/ejemplares")
    @PreAuthorize(PERSONAL)
    public List<EjemplarResponse> ejemplares(@PathVariable Long id) {
        return ejemplarService.listarPorLibro(id);
    }

    @PostMapping("/{id}/ejemplares")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public EjemplarResponse agregarEjemplar(@PathVariable Long id, @Valid @RequestBody EjemplarRequest req) {
        return ejemplarService.crear(id, req);
    }

    // ---------- material digital (PDF / EPUB) ----------

    @PostMapping(value = "/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(BIBLIOTECARIA)
    public LibroResponse subirArchivo(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return libroService.subirArchivo(id, archivo);
    }

    /** Sin parámetros se muestra en el navegador; con descargar=true se baja como archivo. */
    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Long id,
                                            @RequestParam(defaultValue = "false") boolean descargar) {
        ArchivoDescarga a = libroService.obtenerArchivo(id);
        ContentDisposition disposicion = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(a.nombre()).build();
        return ResponseEntity.ok()
                .contentType(a.tipo())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(a.recurso());
    }

    @DeleteMapping("/{id}/archivo")
    @PreAuthorize(BIBLIOTECARIA)
    public LibroResponse quitarArchivo(@PathVariable Long id) {
        return libroService.quitarArchivo(id);
    }
}
