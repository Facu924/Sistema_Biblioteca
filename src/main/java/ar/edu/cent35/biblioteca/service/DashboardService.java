package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.DashboardResponse;
import ar.edu.cent35.biblioteca.dto.DashboardResponse.Actividad;
import ar.edu.cent35.biblioteca.dto.DashboardResponse.CategoriaCantidad;
import ar.edu.cent35.biblioteca.dto.DashboardResponse.LibroDemandado;
import ar.edu.cent35.biblioteca.dto.DashboardResponse.MesFlujo;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Estadísticas del panel principal. Solo consultas de lectura. */
@Service
public class DashboardService {

    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
    private static final List<EstadoPrestamo> ABIERTOS = List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO);

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public DashboardResponse resumen() {
        LocalDate hoy = LocalDate.now();

        long libros = contar("select count(l) from Libro l");
        long ejemplares = contar("select count(e) from EjemplarFisico e where e.estadoEjemplar <> :baja",
                "baja", EstadoEjemplar.BAJA_LOGICA);
        long disponibles = contar("select count(e) from EjemplarFisico e where e.estadoEjemplar = :estado",
                "estado", EstadoEjemplar.DISPONIBLE);
        long abiertos = contar("select count(p) from Prestamo p where p.estadoPrestamo in :estados",
                "estados", ABIERTOS);
        long deHoy = contar("select count(p) from Prestamo p where p.fechaPrestamo >= :inicio",
                "inicio", hoy.atStartOfDay());
        long vencidos = contar("select count(p) from Prestamo p where p.estadoPrestamo in :estados "
                + "and p.fechaLimiteEntrega < :hoy", "estados", ABIERTOS, "hoy", hoy);
        long morosos = contar("select count(distinct p.lector.idLector) from Prestamo p "
                + "where p.estadoPrestamo in :estados and p.fechaLimiteEntrega < :hoy",
                "estados", ABIERTOS, "hoy", hoy);
        long bloqueados = contar("select count(u) from Usuario u where u.cuentaBloqueada = true");
        long reservas = contar("select count(r) from Reserva r where r.estadoReserva = :estado",
                "estado", EstadoReserva.PENDIENTE);
        long penalizaciones = contar("select count(p) from Penalizacion p where p.activa = true "
                + "and (p.fechaFin is null or p.fechaFin >= :hoy)", "hoy", hoy);

        return new DashboardResponse(libros, ejemplares, disponibles, abiertos, deHoy, vencidos,
                tasaDevolucion(hoy), morosos, bloqueados, reservas, penalizaciones,
                flujoMensual(hoy), topLibros(hoy), librosPorCategoria(), actividadReciente());
    }

    // ------------------------------------------------------------ indicadores

    private Double tasaDevolucion(LocalDate hoy) {
        List<Object[]> filas = em.createQuery(
                        "select p.fechaDevolucionReal, p.fechaLimiteEntrega from Prestamo p "
                                + "where p.estadoPrestamo = :estado and p.fechaDevolucionReal >= :desde", Object[].class)
                .setParameter("estado", EstadoPrestamo.DEVUELTO)
                .setParameter("desde", hoy.minusMonths(12).atStartOfDay())
                .getResultList();
        if (filas.isEmpty()) {
            return null;
        }
        long aTiempo = filas.stream()
                .filter(f -> !((LocalDateTime) f[0]).toLocalDate().isAfter((LocalDate) f[1]))
                .count();
        return Math.round(aTiempo * 1000.0 / filas.size()) / 10.0;
    }

    /** Préstamos y devoluciones de los últimos 6 meses, con ceros en los meses sin movimiento. */
    private List<MesFlujo> flujoMensual(LocalDate hoy) {
        YearMonth primero = YearMonth.from(hoy).minusMonths(5);
        LocalDateTime desde = primero.atDay(1).atStartOfDay();

        Map<YearMonth, long[]> meses = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            meses.put(primero.plusMonths(i), new long[2]);
        }
        acumular(meses, 0, "select year(p.fechaPrestamo), month(p.fechaPrestamo), count(p) from Prestamo p "
                + "where p.fechaPrestamo >= :desde group by year(p.fechaPrestamo), month(p.fechaPrestamo)", desde);
        acumular(meses, 1, "select year(h.fechaDevolucionReal), month(h.fechaDevolucionReal), count(h) "
                + "from HistorialTransaccion h where h.fechaDevolucionReal >= :desde "
                + "group by year(h.fechaDevolucionReal), month(h.fechaDevolucionReal)", desde);

        List<MesFlujo> resultado = new ArrayList<>();
        meses.forEach((mes, valores) -> resultado.add(new MesFlujo(
                mes.toString(), MESES[mes.getMonthValue() - 1], valores[0], valores[1])));
        return resultado;
    }

    private void acumular(Map<YearMonth, long[]> meses, int posicion, String jpql, LocalDateTime desde) {
        List<Object[]> filas = em.createQuery(jpql, Object[].class).setParameter("desde", desde).getResultList();
        for (Object[] fila : filas) {
            YearMonth mes = YearMonth.of(((Number) fila[0]).intValue(), ((Number) fila[1]).intValue());
            long[] valores = meses.get(mes);
            if (valores != null) {
                valores[posicion] = ((Number) fila[2]).longValue();
            }
        }
    }

    /** Los 5 libros más prestados en los últimos 30 días. */
    private List<LibroDemandado> topLibros(LocalDate hoy) {
        List<Object[]> filas = em.createQuery(
                        "select lb.titulo, a.nombreAutor, count(p) from Prestamo p "
                                + "join p.ejemplar e join e.libro lb join lb.autor a "
                                + "where p.fechaPrestamo >= :desde "
                                + "group by lb.idLibro, lb.titulo, a.nombreAutor order by count(p) desc", Object[].class)
                .setParameter("desde", hoy.minusDays(30).atStartOfDay())
                .setMaxResults(5)
                .getResultList();
        return filas.stream()
                .map(f -> new LibroDemandado((String) f[0], (String) f[1], ((Number) f[2]).longValue()))
                .toList();
    }

    /** Títulos por materia CDU: las 5 mayores y, si hay más, el resto agrupado como "Otras". */
    private List<CategoriaCantidad> librosPorCategoria() {
        List<Object[]> filas = em.createQuery(
                        "select m.nombreTema, count(l) from Libro l join l.materia m "
                                + "group by m.idMateria, m.nombreTema order by count(l) desc", Object[].class)
                .getResultList();
        List<CategoriaCantidad> resultado = new ArrayList<>();
        long otras = 0;
        for (int i = 0; i < filas.size(); i++) {
            long cantidad = ((Number) filas.get(i)[1]).longValue();
            if (i < 5) {
                resultado.add(new CategoriaCantidad((String) filas.get(i)[0], cantidad));
            } else {
                otras += cantidad;
            }
        }
        if (otras > 0) {
            resultado.add(new CategoriaCantidad("Otras", otras));
        }
        return resultado;
    }

    /** Los últimos 8 movimientos del mostrador, entre préstamos y devoluciones. */
    private List<Actividad> actividadReciente() {
        List<Actividad> todo = new ArrayList<>();
        List<Object[]> prestamos = em.createQuery(
                        "select p.fechaPrestamo, pe.nombre, pe.apellido, lb.titulo from Prestamo p "
                                + "join p.lector l join l.persona pe join p.ejemplar e join e.libro lb "
                                + "order by p.fechaPrestamo desc", Object[].class)
                .setMaxResults(8).getResultList();
        for (Object[] f : prestamos) {
            todo.add(new Actividad("PRESTAMO", (LocalDateTime) f[0], f[1] + " " + f[2], (String) f[3]));
        }
        List<Object[]> devoluciones = em.createQuery(
                        "select h.fechaDevolucionReal, pe.nombre, pe.apellido, lb.titulo from HistorialTransaccion h "
                                + "join h.lector l join l.persona pe join h.ejemplar e join e.libro lb "
                                + "order by h.fechaDevolucionReal desc", Object[].class)
                .setMaxResults(8).getResultList();
        for (Object[] f : devoluciones) {
            todo.add(new Actividad("DEVOLUCION", (LocalDateTime) f[0], f[1] + " " + f[2], (String) f[3]));
        }
        todo.sort(Comparator.comparing(Actividad::fecha).reversed());
        return todo.size() > 8 ? new ArrayList<>(todo.subList(0, 8)) : todo;
    }

    /** Cuenta con parámetros nombrados, pasados de a pares: nombre, valor. */
    private long contar(String jpql, Object... parametros) {
        TypedQuery<Long> consulta = em.createQuery(jpql, Long.class);
        for (int i = 0; i < parametros.length; i += 2) {
            consulta.setParameter((String) parametros[i], parametros[i + 1]);
        }
        return consulta.getSingleResult();
    }
}
