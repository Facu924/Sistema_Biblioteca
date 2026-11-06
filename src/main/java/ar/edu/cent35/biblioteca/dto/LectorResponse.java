package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.TipoLector;

/**
 * Ficha del lector para el mostrador. habilitado es false cuando hoy no se le puede prestar,
 * y motivoNoHabilitado explica por qué (bloqueo, baja, penalización, mora o límite alcanzado).
 */
public record LectorResponse(
        Long idLector,
        String legajo,
        String nombre,
        TipoLector tipoLector,
        String perfilInstitucional,
        String telefono,
        String mail,
        long prestamosAbiertos,
        int limitePrestamos,
        boolean habilitado,
        String motivoNoHabilitado) {
}
