package ar.edu.cent35.biblioteca.model.circulacion;

import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.model.enums.TipoPrestamo;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.cent35.biblioteca.model.inventario.EjemplarFisico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long idPrestamo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private EjemplarFisico ejemplar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    /** Operador que registró la salida. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bibliotecaria", nullable = false)
    private Usuario bibliotecaria;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_limite_entrega", nullable = false)
    private LocalDate fechaLimiteEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_prestamo", nullable = false, length = 20)
    private TipoPrestamo tipoPrestamo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_prestamo", nullable = false, length = 20)
    private EstadoPrestamo estadoPrestamo = EstadoPrestamo.ACTIVO;
}
