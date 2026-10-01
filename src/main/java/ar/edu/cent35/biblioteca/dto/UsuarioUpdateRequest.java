package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.TipoLector;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** contrasena es opcional: si viene, reemplaza la actual (mínimo 8 caracteres). */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 100) String apellido,
        @Size(max = 30) String telefono,
        @NotBlank @Size(max = 30) String legajo,
        @NotBlank @Email @Size(max = 150) String mail,
        @Size(min = 8, max = 72) String contrasena,
        @NotNull TipoUsuario tipoUsuario,
        TipoLector tipoLector,
        @Size(max = 150) String perfilInstitucional) {
}
