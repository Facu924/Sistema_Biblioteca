package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.AutorRequest;
import ar.edu.cent35.biblioteca.dto.AutorResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.inventario.Autor;
import ar.edu.cent35.biblioteca.repository.AutorRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutorService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;

    public AutorService(AutorRepository autorRepository, LibroRepository libroRepository) {
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AutorResponse> listar(String texto, int pagina, int tamanio) {
        Page<Autor> r = autorRepository.findByNombreAutorContaining(
                texto == null ? "" : texto.trim(),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100), Sort.by("nombreAutor")));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AutorResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public AutorResponse crear(AutorRequest req) {
        String nombre = req.nombreAutor().trim();
        if (autorRepository.existsByNombreAutor(nombre)) {
            throw new ReglaNegocioException("Ya existe un autor con ese nombre");
        }
        Autor autor = new Autor();
        autor.setNombreAutor(nombre);
        return aRespuesta(autorRepository.save(autor));
    }

    @Transactional
    public AutorResponse actualizar(Long id, AutorRequest req) {
        Autor autor = buscar(id);
        String nombre = req.nombreAutor().trim();
        if (autorRepository.existsByNombreAutorAndIdAutorNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe un autor con ese nombre");
        }
        autor.setNombreAutor(nombre);
        return aRespuesta(autorRepository.save(autor));
    }

    @Transactional
    public void eliminar(Long id) {
        Autor autor = buscar(id);
        if (libroRepository.existsByAutor_IdAutor(id)) {
            throw new ReglaNegocioException("El autor tiene libros asociados y no puede eliminarse");
        }
        autorRepository.delete(autor);
    }

    private Autor buscar(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado: " + id));
    }

    private AutorResponse aRespuesta(Autor a) {
        return new AutorResponse(a.getIdAutor(), a.getNombreAutor());
    }
}
