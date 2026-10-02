package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.ReservaResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.circulacion.Reserva;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import ar.edu.cent35.biblioteca.model.inventario.Libro;
import ar.edu.cent35.biblioteca.repository.EjemplarFisicoRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import ar.edu.cent35.biblioteca.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reservas con cola de espera: RF-ALU-02 (HU-09). Solo se reserva si no hay ejemplares disponibles. */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LibroRepository libroRepository;
    private final EjemplarFisicoRepository ejemplarRepository;
    private final HabilitacionService habilitacionService;

    public ReservaService(ReservaRepository reservaRepository,
                          LibroRepository libroRepository,
                          EjemplarFisicoRepository ejemplarRepository,
                          HabilitacionService habilitacionService) {
        this.reservaRepository = reservaRepository;
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.habilitacionService = habilitacionService;
    }

    @Transactional
    public ReservaResponse crear(Long idLibro, String actorMail) {
        Lector lector = habilitacionService.lectorDelUsuario(actorMail);
        habilitacionService.verificarPuedeOperar(lector); // HU-09: sin penalización ni bloqueo

        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + idLibro));
        long disponibles = ejemplarRepository.countByLibro_IdLibroAndEstadoEjemplar(idLibro, EstadoEjemplar.DISPONIBLE);
        long prestados = ejemplarRepository.countByLibro_IdLibroAndEstadoEjemplar(idLibro, EstadoEjemplar.PRESTADO);
        if (disponibles > 0) {
            throw new ReglaNegocioException("Hay ejemplares disponibles: no hace falta reservar, puede retirarlo en el mostrador");
        }
        if (prestados == 0) {
            throw new ReglaNegocioException("No hay ejemplares en préstamo de este libro, no se puede reservar");
        }
        if (reservaRepository.existsByLector_IdLectorAndLibro_IdLibroAndEstadoReserva(
                lector.getIdLector(), idLibro, EstadoReserva.PENDIENTE)) {
            throw new ReglaNegocioException("Ya tiene una reserva pendiente de este libro");
        }

        Reserva reserva = new Reserva();
        reserva.setLector(lector);
        reserva.setLibro(libro);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        return aRespuesta(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> mias(String actorMail) {
        Lector lector = habilitacionService.lectorDelUsuario(actorMail);
        return reservaRepository.findByLector_IdLectorOrderByFechaSolicitudDesc(lector.getIdLector())
                .stream().map(this::aRespuesta).toList();
    }

    /** Cola de espera de un libro, en el orden en que debe atenderse. */
    @Transactional(readOnly = true)
    public List<ReservaResponse> colaDelLibro(Long idLibro) {
        if (!libroRepository.existsById(idLibro)) {
            throw new ResourceNotFoundException("Libro no encontrado: " + idLibro);
        }
        return reservaRepository.findByLibro_IdLibroAndEstadoReservaOrderByFechaSolicitudAsc(
                idLibro, EstadoReserva.PENDIENTE).stream().map(this::aRespuesta).toList();
    }

    /** El lector cancela las suyas; el personal puede cancelar cualquiera. */
    @Transactional
    public ReservaResponse cancelar(Long id, String actorMail, boolean esPersonal) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id));
        if (!esPersonal) {
            Lector lector = habilitacionService.lectorDelUsuario(actorMail);
            if (!reserva.getLector().getIdLector().equals(lector.getIdLector())) {
                throw new ReglaNegocioException("Solo puede cancelar sus propias reservas");
            }
        }
        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden cancelar reservas pendientes");
        }
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        return aRespuesta(reservaRepository.save(reserva));
    }

    private ReservaResponse aRespuesta(Reserva r) {
        Persona persona = r.getLector().getPersona();
        Long posicion = null;
        if (r.getEstadoReserva() == EstadoReserva.PENDIENTE) {
            posicion = 1 + reservaRepository.countByLibro_IdLibroAndEstadoReservaAndFechaSolicitudBefore(
                    r.getLibro().getIdLibro(), EstadoReserva.PENDIENTE, r.getFechaSolicitud());
        }
        return new ReservaResponse(r.getIdReserva(), r.getLector().getIdLector(), persona.getLegajo(),
                persona.getNombre() + " " + persona.getApellido(), r.getLibro().getIdLibro(),
                r.getLibro().getTitulo(), r.getFechaSolicitud(), r.getEstadoReserva(), posicion);
    }
}
