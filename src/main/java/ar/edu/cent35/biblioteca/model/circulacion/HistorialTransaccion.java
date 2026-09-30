package ar.edu.cent35.biblioteca.model.circulacion;

import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.enums.EstadoEntrega;
import java.time.LocalDateTime;

import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historial_transacciones")
@Getter
@Setter
@NoArgsConstructor
public class HistorialTransaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private EjemplarFisico ejemplar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bibliotecaria", nullable = false)
    private Usuario bibliotecaria;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_devolucion_real", nullable = false)
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false, length = 30)
    private EstadoEntrega estadoEntrega;

    /** Detalles de daños o incidencias. */
    @Column(length = 500)
    private String observaciones;
}
