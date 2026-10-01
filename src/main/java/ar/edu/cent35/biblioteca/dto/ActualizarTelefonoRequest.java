package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.Size;

public record ActualizarTelefonoRequest(@Size(max = 30) String telefono) {
}
