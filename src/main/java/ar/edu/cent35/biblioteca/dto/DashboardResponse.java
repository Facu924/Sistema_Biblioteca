package ar.edu.cent35.biblioteca.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos del panel principal. Definiciones:
 *  - ejemplaresTotal no cuenta los dados de baja.
 *  - prestamosAbiertos suma los ACTIVO y los VENCIDO.
 *  - prestamosVencidos y lectoresMorosos se calculan por fecha límite, no por el estado guardado.
 *  - tasaDevolucionATiempo es el porcentaje de devoluciones en fecha de los últimos 12 meses (null si no hay).
 *  - librosPorCategoria agrupa los títulos por materia CDU: las 5 mayores y el resto como "Otras".
 */
public record DashboardResponse(
        long librosEnCatalogo,
        long ejemplaresTotal,
        long ejemplaresDisponibles,
        long prestamosAbiertos,
        long prestamosHoy,
        long prestamosVencidos,
        Double tasaDevolucionATiempo,
        long lectoresMorosos,
        long usuariosBloqueados,
        long reservasPendientes,
        long penalizacionesVigentes,
        List<MesFlujo> flujoMensual,
        List<LibroDemandado> topLibros,
        List<CategoriaCantidad> librosPorCategoria,
        List<Actividad> actividadReciente) {

    public record MesFlujo(String mes, String etiqueta, long prestamos, long devoluciones) {
    }

    public record LibroDemandado(String titulo, String autor, long cantidad) {
    }

    public record CategoriaCantidad(String categoria, long cantidad) {
    }

    /** tipo: PRESTAMO o DEVOLUCION. */
    public record Actividad(String tipo, LocalDateTime fecha, String lector, String libro) {
    }
}
