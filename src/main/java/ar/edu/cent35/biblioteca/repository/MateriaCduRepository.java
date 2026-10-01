package ar.edu.cent35.biblioteca.repository;

import ar.edu.cent35.biblioteca.model.inventario.MateriaCdu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaCduRepository extends JpaRepository<MateriaCdu, Long> {

    Page<MateriaCdu> findByCodigoCduContainingOrNombreTemaContaining(String codigo, String tema, Pageable pageable);

    boolean existsByCodigoCdu(String codigoCdu);

    boolean existsByCodigoCduAndIdMateriaNot(String codigoCdu, Long idMateria);
}
