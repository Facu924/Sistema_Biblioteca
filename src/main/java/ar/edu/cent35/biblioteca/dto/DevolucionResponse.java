package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoEntrega;

/** reservasPendientesDelLibro avisa a la bibliotecaria si hay alguien esperando este título. */
public record DevolucionResponse(
        PrestamoResponse prestamo,
        EstadoEntrega estadoEntrega,
        long diasAtraso,
        PenalizacionResponse penalizacion,
        long reservasPendientesDelLibro) {
}
