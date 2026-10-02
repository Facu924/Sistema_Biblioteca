package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.repository.PenalizacionRepository;
import ar.edu.cent35.biblioteca.repository.PrestamoRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Mantenimiento diario: marca préstamos vencidos y desactiva penalizaciones cumplidas. */
@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final PrestamoRepository prestamoRepository;
    private final PenalizacionRepository penalizacionRepository;

    public TareasProgramadas(PrestamoRepository prestamoRepository, PenalizacionRepository penalizacionRepository) {
        this.prestamoRepository = prestamoRepository;
        this.penalizacionRepository = penalizacionRepository;
    }

    /** Todos los días a las 00:05. */
    @Scheduled(cron = "0 5 0 * * *")
    public void mantenimientoDiario() {
        LocalDate hoy = LocalDate.now();
        int vencidos = prestamoRepository.marcarVencidos(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO, hoy);
        int penalizaciones = penalizacionRepository.vencer(hoy);
        log.info("Mantenimiento diario: {} préstamos marcados como vencidos, {} penalizaciones cumplidas",
                vencidos, penalizaciones);
    }

    /** Al arrancar, por si la app estuvo apagada a la hora programada. */
    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        mantenimientoDiario();
    }
}
