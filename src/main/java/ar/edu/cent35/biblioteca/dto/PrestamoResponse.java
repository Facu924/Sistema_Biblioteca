package ar.edu.cent35.biblioteca.dto;

import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.model.enums.TipoPrestamo;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** telefono y mail solo se completan en el listado de mora para el personal (RF-BIB-07). */
public record PrestamoResponse(
        Long idPrestamo,
        Long idEjemplar,
        String numeroInventario,
        Long idLibro,
        String tituloLibro,
        String isbn,
        Long idLector,
        String legajo,
        String nombreLector,
        TipoPrestamo tipoPrestamo,
        EstadoPrestamo estadoPrestamo,
        LocalDateTime fechaPrestamo,
        LocalDate fechaLimiteEntrega,
        LocalDateTime fechaDevolucionReal,
        long diasAtraso,
        String telefono,
        String mail) {
}
