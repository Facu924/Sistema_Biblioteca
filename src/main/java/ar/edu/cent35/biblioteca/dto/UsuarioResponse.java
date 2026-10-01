package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.TipoLector;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;

/** Nunca incluye la contraseña. tipoLector y perfilInstitucional son null si no es lector. */
public record UsuarioResponse(
        Long idUsuario,
        Long idPersona,
        String nombre,
        String apellido,
        String telefono,
        String legajo,
        String mail,
        TipoUsuario tipoUsuario,
        TipoLector tipoLector,
        String perfilInstitucional,
        Boolean activo,
        Boolean cuentaBloqueada,
        Integer intentosFallidos) {
}
