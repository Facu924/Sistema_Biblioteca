package ar.edu.cent35.biblioteca.dto;

/** Números generales del catálogo, visibles sin iniciar sesión. */
public record ResumenPublicoResponse(long libros, long ejemplaresDisponibles, long librosDigitales) {
}
