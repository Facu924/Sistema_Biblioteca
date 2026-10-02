package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoEntrega;
import java.time.LocalDateTime;

public record HistorialResponse(
        Long idHistorial,
        String numeroInventario,
        String tituloLibro,
        String legajo,
        String nombreLector,
        LocalDateTime fechaPrestamo,
        LocalDateTime fechaDevolucionReal,
        EstadoEntrega estadoEntrega,
        String observaciones) {
}
