package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EjemplarRequest(
        @NotBlank @Size(max = 50) String numeroInventario,
        @Size(max = 100) String ubicacionEstante) {
}
