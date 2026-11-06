package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.ResumenPublicoResponse;
import ar.edu.cent35.biblioteca.model.enums.EstadoEjemplar;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumenPublicoService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public ResumenPublicoResponse resumen() {
        long libros = em.createQuery("select count(l) from Libro l", Long.class).getSingleResult();
        long disponibles = em.createQuery(
                        "select count(e) from EjemplarFisico e where e.estadoEjemplar = :estado", Long.class)
                .setParameter("estado", EstadoEjemplar.DISPONIBLE).getSingleResult();
        long digitales = em.createQuery(
                "select count(l) from Libro l where l.esDigital = true", Long.class).getSingleResult();
        return new ResumenPublicoResponse(libros, disponibles, digitales);
    }
}
