package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoEntrega;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * observaciones es obligatoria si hay daño o pérdida.
 * diasPenalizacion es opcional y lo decide la bibliotecaria:
 *  - sin valor: atraso => un día por cada día de atraso; daño o pérdida => hasta que se levante a mano.
 *  - 0: no se aplica penalización.
 *  - N: penalización de N días.
 */
public record DevolucionRequest(
        @NotBlank String numeroInventario,
        @NotNull EstadoEntrega estadoEntrega,
        @Size(max = 300) String observaciones,
        @Min(0) @Max(365) Integer diasPenalizacion) {
}
