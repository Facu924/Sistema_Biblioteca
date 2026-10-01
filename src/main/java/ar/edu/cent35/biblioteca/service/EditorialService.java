package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.EditorialRequest;
import ar.edu.cent35.biblioteca.dto.EditorialResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.inventario.Editorial;
import ar.edu.cent35.biblioteca.repository.EditorialRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditorialService {

    private final EditorialRepository editorialRepository;
    private final LibroRepository libroRepository;

    public EditorialService(EditorialRepository editorialRepository, LibroRepository libroRepository) {
        this.editorialRepository = editorialRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EditorialResponse> listar(String texto, int pagina, int tamanio) {
        Page<Editorial> r = editorialRepository.findByNombreEditorialContaining(
                texto == null ? "" : texto.trim(),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100), Sort.by("nombreEditorial")));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EditorialResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public EditorialResponse crear(EditorialRequest req) {
        String nombre = req.nombreEditorial().trim();
        if (editorialRepository.existsByNombreEditorial(nombre)) {
            throw new ReglaNegocioException("Ya existe una editorial con ese nombre");
        }
        Editorial e = new Editorial();
        e.setNombreEditorial(nombre);
        return aRespuesta(editorialRepository.save(e));
    }

    @Transactional
    public EditorialResponse actualizar(Long id, EditorialRequest req) {
        Editorial e = buscar(id);
        String nombre = req.nombreEditorial().trim();
        if (editorialRepository.existsByNombreEditorialAndIdEditorialNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una editorial con ese nombre");
        }
        e.setNombreEditorial(nombre);
        return aRespuesta(editorialRepository.save(e));
    }

    @Transactional
    public void eliminar(Long id) {
        Editorial e = buscar(id);
        if (libroRepository.existsByEditorial_IdEditorial(id)) {
            throw new ReglaNegocioException("La editorial tiene libros asociados y no puede eliminarse");
        }
        editorialRepository.delete(e);
    }

    private Editorial buscar(Long id) {
        return editorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial no encontrada: " + id));
    }

    private EditorialResponse aRespuesta(Editorial e) {
        return new EditorialResponse(e.getIdEditorial(), e.getNombreEditorial());
    }
}
