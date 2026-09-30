package ar.edu.cent35.biblioteca.dto;

/** claveTemporal viene en null si no se pidió blanqueo. Se muestra una sola vez. */
public record DesbloqueoResponse(Long idUsuario, String claveTemporal) {
}
