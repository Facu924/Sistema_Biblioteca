package ar.edu.cent35.biblioteca.dto;

import java.time.LocalDate;

/** vigente = activa y sin fecha de fin vencida. */
public record PenalizacionResponse(
        Long idPenalizacion,
        Long idLector,
        String legajo,
        String nombreLector,
        String motivo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Boolean activa,
        boolean vigente) {
}
