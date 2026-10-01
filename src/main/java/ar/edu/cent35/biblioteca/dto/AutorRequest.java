package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formato sugerido: "Apellido, Nombre". */
public record AutorRequest(@NotBlank @Size(max = 200) String nombreAutor) {
}
