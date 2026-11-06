package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.PrestamoRequest;
import ar.edu.cent35.biblioteca.dto.PrestamoResponse;
import ar.edu.cent35.biblioteca.dto.ResumenPrestamosResponse;
import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.circulacion.Prestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import ar.edu.cent35.biblioteca.model.enums.TipoPrestamo;
import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import ar.edu.cent35.biblioteca.repository.EjemplarFisicoRepository;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.LibroRepository;
import ar.edu.cent35.biblioteca.repository.PrestamoRepository;
import ar.edu.cent35.biblioteca.repository.ReservaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Registro de préstamos en el mostrador: RF-BIB-03, RF-BIB-04 y RF-BIB-05 (HU-04, HU-05, HU-12, HU-13). */
@Service
public class PrestamoService {

    /** Préstamos que todavía no volvieron: cuentan para el límite. */
    static final Set<EstadoPrestamo> ABIERTOS = EnumSet.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO);

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final EjemplarFisicoRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilitacionService habilitacionService;
    private final int plazoDomicilioDias;
    private final int limitePorLector;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           LectorRepository lectorRepository,
                           EjemplarFisicoRepository ejemplarRepository,
                           LibroRepository libroRepository,
                           ReservaRepository reservaRepository,
                           UsuarioRepository usuarioRepository,
                           HabilitacionService habilitacionService,
                           @Value("${app.prestamos.plazo-domicilio-dias:7}") int plazoDomicilioDias,
                           @Value("${app.prestamos.limite-por-lector:2}") int limitePorLector) {
        this.prestamoRepository = prestamoRepository;
        this.lectorRepository = lectorRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.habilitacionService = habilitacionService;
        this.plazoDomicilioDias = plazoDomicilioDias;
        this.limitePorLector = limitePorLector;
    }

    /** Todo ocurre en una transacción: si algo falla, no queda nada a medias (RNF-REL-02). */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PrestamoResponse registrar(PrestamoRequest req, String actorMail) {
        LocalDate hoy = LocalDate.now();
        Usuario bibliotecaria = usuarioRepository.findByMail(actorMail)
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado"));
        Lector lector = lectorRepository.findByPersona_Legajo(req.legajo().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un lector con ese legajo"));

        // HU-05: usuario habilitado, sin bloqueo ni penalizaciones, y sin mora
        habilitacionService.verificarPuedeOperar(lector);
        if (prestamoRepository.contarEnMora(lector.getIdLector(), ABIERTOS, hoy) > 0) {
            throw new ReglaNegocioException("El lector tiene un préstamo vencido sin devolver");
        }
        // HU-13: límite de materiales simultáneos
        if (prestamoRepository.countByLector_IdLectorAndEstadoPrestamoIn(lector.getIdLector(), ABIERTOS)
                >= limitePorLector) {
            throw new ReglaNegocioException("El lector ya tiene " + limitePorLector + " materiales en préstamo");
        }

        String inventario = req.numeroInventario().trim();
        EjemplarFisico preliminar = ejemplarRepository.findByNumeroInventario(inventario)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ejemplar con inventario " + inventario));
        Long idLibro = preliminar.getLibro().getIdLibro();
        // Bloqueos para que dos préstamos simultáneos no rompan el stock mínimo ni presten el mismo ejemplar
        libroRepository.findByIdParaActualizar(idLibro);
        EjemplarFisico ejemplar = ejemplarRepository.findByNumeroInventarioParaActualizar(inventario)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ejemplar con inventario " + inventario));

        if (ejemplar.getEstadoEjemplar() != EstadoEjemplar.DISPONIBLE) {
            throw new ReglaNegocioException("El ejemplar no está disponible (estado: " + ejemplar.getEstadoEjemplar() + ")");
        }
        // HU-12: stock mínimo, siempre queda un ejemplar para consulta en sala
        if (req.tipoPrestamo() == TipoPrestamo.DOMICILIO) {
            long disponibles = ejemplarRepository
                    .countByLibro_IdLibroAndEstadoEjemplar(idLibro, EstadoEjemplar.DISPONIBLE);
            if (disponibles <= 1) {
                throw new ReglaNegocioException(
                        "Solo queda un ejemplar disponible: se reserva para consulta en Aula o Sala");
            }
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setEjemplar(ejemplar);
        prestamo.setLector(lector);
        prestamo.setBibliotecaria(bibliotecaria);
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setTipoPrestamo(req.tipoPrestamo());
        prestamo.setFechaLimiteEntrega(
                req.tipoPrestamo() == TipoPrestamo.DOMICILIO ? hoy.plusDays(plazoDomicilioDias) : hoy);
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
        prestamo = prestamoRepository.save(prestamo);

        ejemplar.setEstadoEjemplar(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);

        // Si el lector tenía una reserva pendiente de este título, queda completada
        reservaRepository.findFirstByLector_IdLectorAndLibro_IdLibroAndEstadoReservaOrderByFechaSolicitudAsc(
                lector.getIdLector(), idLibro, EstadoReserva.PENDIENTE)
                .ifPresent(r -> {
                    r.setEstadoReserva(EstadoReserva.COMPLETADA);
                    reservaRepository.save(r);
                });

        return aRespuesta(prestamo);
    }

    /** El préstamo abierto de un ejemplar: sirve para ver el atraso antes de registrar la devolución. */
    @Transactional(readOnly = true)
    public PrestamoResponse abiertoPorInventario(String numero) {
        return aRespuesta(prestamoRepository.findAbiertoPorInventario(numero.trim(), ABIERTOS)
                .orElseThrow(() -> new ResourceNotFoundException("Ese ejemplar no tiene un préstamo abierto")));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PrestamoResponse> listar(EstadoPrestamo estado, String texto, int pagina, int tamanio) {
        Page<Prestamo> r = prestamoRepository.buscar(estado, texto == null ? "" : texto.trim(),
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100)));
        return new PaginaResponse<>(r.getContent().stream().map(this::aRespuesta).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    /** RF-BIB-07: préstamos en mora con teléfono y correo del lector para poder contactarlo. */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> enMora() {
        List<Prestamo> lista = prestamoRepository.findEnMora(ABIERTOS, LocalDate.now());
        List<Long> idsPersona = lista.stream().map(p -> p.getLector().getPersona().getIdPersona()).distinct().toList();
        Map<Long, String> mails = new HashMap<>();
        if (!idsPersona.isEmpty()) {
            mails = usuarioRepository.findByPersona_IdPersonaIn(idsPersona).stream()
                    .collect(Collectors.toMap(u -> u.getPersona().getIdPersona(), Usuario::getMail, (a, b) -> a));
        }
        Map<Long, String> mailsPorPersona = mails;
        return lista.stream().map(p -> {
            Persona persona = p.getLector().getPersona();
            return aRespuesta(p, persona.getTelefono(), mailsPorPersona.get(persona.getIdPersona()));
        }).toList();
    }

    /** RF-ALU-03: solo los préstamos del usuario con sesión iniciada. */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> mios(String mail) {
        Lector lector = habilitacionService.lectorDelUsuario(mail);
        return prestamoRepository.findDelLector(lector.getIdLector()).stream().map(this::aRespuesta).toList();
    }

    /** Totales por estado para las tarjetas de la pantalla. */
    @Transactional(readOnly = true)
    public ResumenPrestamosResponse resumen() {
        return new ResumenPrestamosResponse(
                prestamoRepository.countByEstadoPrestamo(EstadoPrestamo.ACTIVO),
                prestamoRepository.countByEstadoPrestamo(EstadoPrestamo.DEVUELTO),
                prestamoRepository.countByEstadoPrestamo(EstadoPrestamo.VENCIDO));
    }

    public PrestamoResponse aRespuesta(Prestamo p) {
        return aRespuesta(p, null, null);
    }

    public PrestamoResponse aRespuesta(Prestamo p, String telefono, String mail) {
        Persona persona = p.getLector().getPersona();
        LocalDate referencia = p.getFechaDevolucionReal() != null
                ? p.getFechaDevolucionReal().toLocalDate() : LocalDate.now();
        long atraso = Math.max(0, ChronoUnit.DAYS.between(p.getFechaLimiteEntrega(), referencia));
        return new PrestamoResponse(
                p.getIdPrestamo(), p.getEjemplar().getIdEjemplar(), p.getEjemplar().getNumeroInventario(),
                p.getEjemplar().getLibro().getIdLibro(), p.getEjemplar().getLibro().getTitulo(),
                p.getEjemplar().getLibro().getIsbn(),
                p.getLector().getIdLector(), persona.getLegajo(), persona.getNombre() + " " + persona.getApellido(),
                p.getTipoPrestamo(), p.getEstadoPrestamo(), p.getFechaPrestamo(), p.getFechaLimiteEntrega(),
                p.getFechaDevolucionReal(), atraso, telefono, mail);
    }
}
