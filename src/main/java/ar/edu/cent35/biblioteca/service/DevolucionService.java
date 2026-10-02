package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.DevolucionRequest;
import ar.edu.cent35.biblioteca.dto.DevolucionResponse;
import ar.edu.cent35.biblioteca.dto.PenalizacionResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.circulacion.HistorialTransaccion;
import ar.edu.cent35.biblioteca.model.circulacion.Penalizacion;
import ar.edu.cent35.biblioteca.model.circulacion.Prestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.enums.EstadoEntrega;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import ar.edu.cent35.biblioteca.repository.EjemplarFisicoRepository;
import ar.edu.cent35.biblioteca.repository.HistorialTransaccionRepository;
import ar.edu.cent35.biblioteca.repository.PrestamoRepository;
import ar.edu.cent35.biblioteca.repository.ReservaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Devoluciones: RF-BIB-06 (HU-06). Actualiza el stock, registra el historial y decide la penalización. */
@Service
public class DevolucionService {

    private final PrestamoRepository prestamoRepository;
    private final EjemplarFisicoRepository ejemplarRepository;
    private final HistorialTransaccionRepository historialRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PenalizacionService penalizacionService;
    private final PrestamoService prestamoService;

    public DevolucionService(PrestamoRepository prestamoRepository,
                             EjemplarFisicoRepository ejemplarRepository,
                             HistorialTransaccionRepository historialRepository,
                             ReservaRepository reservaRepository,
                             UsuarioRepository usuarioRepository,
                             PenalizacionService penalizacionService,
                             PrestamoService prestamoService) {
        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.historialRepository = historialRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.penalizacionService = penalizacionService;
        this.prestamoService = prestamoService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DevolucionResponse devolver(DevolucionRequest req, String actorMail, String ip) {
        String inventario = req.numeroInventario().trim();
        EstadoEntrega estado = req.estadoEntrega();
        String observaciones = (req.observaciones() == null || req.observaciones().isBlank())
                ? null : req.observaciones().trim();
        if (estado != EstadoEntrega.DEVUELTO_OK && observaciones == null) {
            throw new ReglaNegocioException("Indique en las observaciones qué ocurrió con el ejemplar");
        }

        Usuario bibliotecaria = usuarioRepository.findByMail(actorMail)
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado"));

        // Se bloquea el ejemplar para que no se devuelva dos veces a la vez
        EjemplarFisico ejemplar = ejemplarRepository.findByNumeroInventarioParaActualizar(inventario)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ejemplar con inventario " + inventario));
        Prestamo prestamo = prestamoRepository.findAbiertoPorInventario(inventario, PrestamoService.ABIERTOS)
                .orElseThrow(() -> new ResourceNotFoundException("Ese ejemplar no tiene un préstamo abierto"));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();
        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(prestamo.getFechaLimiteEntrega(), hoy));

        // Registro permanente de la transacción
        HistorialTransaccion historial = new HistorialTransaccion();
        historial.setEjemplar(ejemplar);
        historial.setLector(prestamo.getLector());
        historial.setBibliotecaria(bibliotecaria);
        historial.setFechaPrestamo(prestamo.getFechaPrestamo());
        historial.setFechaDevolucionReal(ahora);
        historial.setEstadoEntrega(estado);
        historial.setObservaciones(observaciones);
        historial = historialRepository.save(historial);

        // Cierre del préstamo y actualización inmediata del stock
        prestamo.setEstadoPrestamo(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(ahora);
        prestamoRepository.save(prestamo);

        ejemplar.setEstadoEjemplar(switch (estado) {
            case DEVUELTO_OK -> EstadoEjemplar.DISPONIBLE;
            case DEVUELTO_CON_DANO -> EstadoEjemplar.DANADO;
            case PERDIDO -> EstadoEjemplar.EXTRAVIADO;
        });
        ejemplarRepository.save(ejemplar);

        Penalizacion penalizacion = penalizarSiCorresponde(req, prestamo, historial, estado,
                diasAtraso, observaciones, hoy, bibliotecaria, ip);

        long reservasPendientes = reservaRepository.countByLibro_IdLibroAndEstadoReserva(
                ejemplar.getLibro().getIdLibro(), EstadoReserva.PENDIENTE);

        PenalizacionResponse penalizacionRespuesta =
                penalizacion == null ? null : penalizacionService.aRespuesta(penalizacion);
        return new DevolucionResponse(prestamoService.aRespuesta(prestamo), estado, diasAtraso,
                penalizacionRespuesta, reservasPendientes);
    }

    /**
     * Regla flexible: el sistema sugiere, la bibliotecaria decide con diasPenalizacion.
     * Atraso: sugerido = un día por día de atraso. Daño o pérdida: sin fecha de fin hasta que se levante.
     * diasPenalizacion = 0 anula la sanción; un número N fija N días.
     */
    private Penalizacion penalizarSiCorresponde(DevolucionRequest req, Prestamo prestamo,
                                                HistorialTransaccion historial, EstadoEntrega estado,
                                                long diasAtraso, String observaciones, LocalDate hoy,
                                                Usuario actor, String ip) {
        List<String> motivos = new ArrayList<>();
        if (diasAtraso > 0) {
            motivos.add("Atraso de " + diasAtraso + (diasAtraso == 1 ? " día" : " días"));
        }
        if (estado == EstadoEntrega.DEVUELTO_CON_DANO) {
            motivos.add("Devolución con daño");
        }
        if (estado == EstadoEntrega.PERDIDO) {
            motivos.add("Material extraviado");
        }
        if (motivos.isEmpty()) {
            return null;
        }

        boolean indefinida = estado != EstadoEntrega.DEVUELTO_OK;
        Integer dias = req.diasPenalizacion();
        LocalDate fin = null;
        if (dias != null) {
            if (dias == 0) {
                return null; // la bibliotecaria decidió no penalizar
            }
            fin = hoy.plusDays(dias);
        } else if (!indefinida) {
            fin = hoy.plusDays(diasAtraso);
        }

        String motivo = String.join(". ", motivos) + (observaciones == null ? "" : ". " + observaciones);
        return penalizacionService.crear(prestamo.getLector(), historial, motivo, hoy, fin, actor, ip);
    }
}
