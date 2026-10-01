package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** El ISBN es opcional (hay material sin ISBN); si viene, debe tener 10 o 13 caracteres. */
public record LibroRequest(
        @Size(max = 20) String isbn,
        @NotBlank @Size(max = 300) String titulo,
        @NotNull Long idAutor,
        @NotNull Long idEditorial,
        @NotNull Long idMateria,
        @Min(1000) @Max(2100) Integer anioPublicacion,
        @Min(1) Integer numeroPaginas,
        @Size(max = 100) String edicion,
        @Size(max = 5000) String resumen) {
}
