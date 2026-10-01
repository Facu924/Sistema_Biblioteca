package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;

public record EjemplarResponse(
        Long idEjemplar,
        Long idLibro,
        String tituloLibro,
        String numeroInventario,
        String ubicacionEstante,
        EstadoEjemplar estadoEjemplar) {
}
