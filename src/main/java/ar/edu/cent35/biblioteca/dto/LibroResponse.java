package ar.edu.cent35.biblioteca.dto;

/**
 * ejemplaresTotal no cuenta los dados de baja; ejemplaresDisponibles cuenta solo los DISPONIBLE.
 * prestamoDomicilioHabilitado es false cuando queda un solo ejemplar disponible (RF-BIB-04).
 */
public record LibroResponse(
        Long idLibro,
        String isbn,
        String titulo,
        Long idAutor,
        String nombreAutor,
        Long idEditorial,
        String nombreEditorial,
        Long idMateria,
        String codigoCdu,
        String nombreTema,
        Integer anioPublicacion,
        Integer numeroPaginas,
        String edicion,
        String resumen,
        Boolean esDigital,
        long ejemplaresTotal,
        long ejemplaresDisponibles,
        boolean prestamoDomicilioHabilitado) {
}
