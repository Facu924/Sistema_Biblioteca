package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import java.time.LocalDateTime;

/** posicionEnCola solo tiene valor mientras la reserva está PENDIENTE (1 = el próximo en ser atendido). */
public record ReservaResponse(
        Long idReserva,
        Long idLector,
        String legajo,
        String nombreLector,
        Long idLibro,
        String tituloLibro,
        LocalDateTime fechaSolicitud,
        EstadoReserva estadoReserva,
        Long posicionEnCola) {
}
