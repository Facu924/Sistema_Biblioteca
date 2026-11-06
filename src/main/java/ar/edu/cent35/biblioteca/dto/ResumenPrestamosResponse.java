package ar.edu.cent35.biblioteca.dto;

/** Totales por estado, para las tarjetas de la pantalla de préstamos. */
public record ResumenPrestamosResponse(long activos, long devueltos, long vencidos) {
}
