package ar.edu.cent35.biblioteca.model.inventario;

import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ejemplar_fisico")
@Getter
@Setter
@NoArgsConstructor
public class EjemplarFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejemplar")
    private Long idEjemplar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    /** Clave para la lectura por código de barras. */
    @Column(name = "numero_inventario", nullable = false, unique = true, length = 50)
    private String numeroInventario;

    @Column(name = "ubicacion_estante", length = 100)
    private String ubicacionEstante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ejemplar", nullable = false, length = 20)
    private EstadoEjemplar estadoEjemplar = EstadoEjemplar.DISPONIBLE;
}
