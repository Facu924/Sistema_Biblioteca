package ar.edu.cent35.biblioteca.model.inventario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "materia_cdu")
@Getter
@Setter
@NoArgsConstructor
public class MateriaCdu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materia")
    private Long idMateria;

    /** Ej: 004.7, 821.134 */
    @Column(name = "codigo_cdu", nullable = false, length = 30)
    private String codigoCdu;

    @Column(name = "nombre_tema", nullable = false, length = 200)
    private String nombreTema;
}
