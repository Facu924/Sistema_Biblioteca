package ar.edu.cent35.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarMailRequest(
        @NotBlank String contrasenaActual,
        @NotBlank @Email @Size(max = 150) String mail) {
}
