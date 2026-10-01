package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Las observaciones (motivo) son obligatorias para DANADO, EXTRAVIADO y BAJA_LOGICA. */
public record CambioEstadoRequest(
        @NotNull EstadoEjemplar estado,
        @Size(max = 300) String observaciones) {
}
