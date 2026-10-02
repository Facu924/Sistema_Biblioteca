package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.TipoPrestamo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** El lector se identifica por su legajo y el ejemplar por el número de inventario (código de barras). */
public record PrestamoRequest(
        @NotBlank String legajo,
        @NotBlank String numeroInventario,
        @NotNull TipoPrestamo tipoPrestamo) {
}
