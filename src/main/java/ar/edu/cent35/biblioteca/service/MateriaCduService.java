package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.MateriaCduRequest;
import ar.edu.cent35.biblioteca.dto.MateriaCduResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.inventario.MateriaCdu;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import ar.edu.cent35.biblioteca.repository.MateriaCduRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MateriaCduService {

    private final MateriaCduRepository materiaRepository;
    private final LibroRepository libroRepository;

    public MateriaCduService(MateriaCduRepository materiaRepository, LibroRepository libroRepository) {
        this.materiaRepository = materiaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MateriaCduResponse> listar(String texto, int pagina, int tamanio) {
        String t = texto == null ? "" : texto.trim();
        Page<MateriaCdu> r = materiaRepository.findByCodigoCduContainingOrNombreTemaContaining(
                t, t, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100), Sort.by("codigoCdu")));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public MateriaCduResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public MateriaCduResponse crear(MateriaCduRequest req) {
        String codigo = req.codigoCdu().trim();
        if (materiaRepository.existsByCodigoCdu(codigo)) {
            throw new ReglaNegocioException("Ya existe una materia con ese código CDU");
        }
        MateriaCdu m = new MateriaCdu();
        m.setCodigoCdu(codigo);
        m.setNombreTema(req.nombreTema().trim());
        return aRespuesta(materiaRepository.save(m));
    }

    @Transactional
    public MateriaCduResponse actualizar(Long id, MateriaCduRequest req) {
        MateriaCdu m = buscar(id);
        String codigo = req.codigoCdu().trim();
        if (materiaRepository.existsByCodigoCduAndIdMateriaNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe una materia con ese código CDU");
        }
        m.setCodigoCdu(codigo);
        m.setNombreTema(req.nombreTema().trim());
        return aRespuesta(materiaRepository.save(m));
    }

    @Transactional
    public void eliminar(Long id) {
        MateriaCdu m = buscar(id);
        if (libroRepository.existsByMateria_IdMateria(id)) {
            throw new ReglaNegocioException("La materia tiene libros asociados y no puede eliminarse");
        }
        materiaRepository.delete(m);
    }

    private MateriaCdu buscar(Long id) {
        return materiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + id));
    }

    private MateriaCduResponse aRespuesta(MateriaCdu m) {
        return new MateriaCduResponse(m.getIdMateria(), m.getCodigoCdu(), m.getNombreTema());
    }
}
