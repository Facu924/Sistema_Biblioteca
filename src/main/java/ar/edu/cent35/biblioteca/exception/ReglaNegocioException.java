package ar.edu.cent35.biblioteca.exception;

/** Se lanza cuando se viola una regla de negocio (penalización, límite de préstamos, stock mínimo...). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
