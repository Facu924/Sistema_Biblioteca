package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sanción manual. Sin dias queda vigente hasta que la bibliotecaria la levante. */
public record PenalizacionRequest(
        @NotBlank String legajo,
        @NotBlank @Size(max = 300) String motivo,
        @Min(1) @Max(365) Integer dias) {
}
