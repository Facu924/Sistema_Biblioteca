package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditorialRequest(@NotBlank @Size(max = 200) String nombreEditorial) {
}
