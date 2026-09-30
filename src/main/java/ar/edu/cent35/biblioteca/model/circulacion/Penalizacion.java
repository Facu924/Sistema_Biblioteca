package ar.edu.cent35.biblioteca.model.circulacion;

import java.time.LocalDate;

import ar.edu.cent35.biblioteca.model.acceso.Lector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "penalizacion")
@Getter
@Setter
@NoArgsConstructor
public class Penalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penalizacion")
    private Long idPenalizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    /** Transacción que originó la sanción (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historial")
    private HistorialTransaccion historial;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Boolean activa = true;
}
