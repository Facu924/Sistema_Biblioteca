package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(@NotNull Long idLibro) {
}
