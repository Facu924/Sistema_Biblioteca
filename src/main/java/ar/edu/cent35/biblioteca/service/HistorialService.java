package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.HistorialResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.circulacion.HistorialTransaccion;
import ar.edu.cent35.biblioteca.repository.HistorialTransaccionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Auditoría de devoluciones y transacciones cerradas: RF-BIB-07 y RF-ADM-06. */
@Service
public class HistorialService {

    private final HistorialTransaccionRepository historialRepository;

    public HistorialService(HistorialTransaccionRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<HistorialResponse> listar(String legajo, String inventario, int pagina, int tamanio) {
        Page<HistorialTransaccion> r = historialRepository.buscar(
                legajo == null ? "" : legajo.trim(), inventario == null ? "" : inventario.trim(),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100)));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    private HistorialResponse aRespuesta(HistorialTransaccion h) {
        Persona p = h.getLector().getPersona();
        return new HistorialResponse(h.getIdHistorial(), h.getEjemplar().getNumeroInventario(),
                h.getEjemplar().getLibro().getTitulo(), p.getLegajo(), p.getNombre() + " " + p.getApellido(),
                h.getFechaPrestamo(), h.getFechaDevolucionReal(), h.getEstadoEntrega(), h.getObservaciones());
    }
}
