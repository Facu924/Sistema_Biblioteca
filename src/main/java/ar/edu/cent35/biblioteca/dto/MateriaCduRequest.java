package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MateriaCduRequest(
        @NotBlank @Size(max = 30) String codigoCdu,
        @NotBlank @Size(max = 200) String nombreTema) {
}
