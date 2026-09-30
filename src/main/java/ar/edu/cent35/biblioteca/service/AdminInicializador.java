package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import ar.edu.cent35.biblioteca.repository.PersonaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el administrador inicial al arrancar, solo si todavía no existe ninguno.
 * Las credenciales vienen de variables de entorno (RNF-SEC-03): ADMIN_MAIL y ADMIN_PASSWORD.
 */
@Component
public class AdminInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);
    private static final String LEGAJO_ADMIN = "ADMIN-INICIAL";

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminMail;
    private final String adminPassword;

    public AdminInicializador(UsuarioRepository usuarioRepository,
                              PersonaRepository personaRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.mail:}") String adminMail,
                              @Value("${app.admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminMail = adminMail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByTipoUsuario(TipoUsuario.ROLE_ADMIN)) {
            return;
        }
        if (adminMail.isBlank() || adminPassword.length() < 8) {
            log.warn("No hay administrador y faltan ADMIN_MAIL / ADMIN_PASSWORD (mínimo 8 caracteres). No se creó ninguno.");
            return;
        }

        Persona persona = personaRepository.findByLegajo(LEGAJO_ADMIN).orElseGet(() -> {
            Persona p = new Persona();
            p.setNombre("Administrador");
            p.setApellido("Sistema");
            p.setLegajo(LEGAJO_ADMIN);
            return personaRepository.save(p);
        });

        Usuario admin = new Usuario();
        admin.setPersona(persona);
        admin.setMail(adminMail);
        admin.setContrasena(passwordEncoder.encode(adminPassword));
        admin.setTipoUsuario(TipoUsuario.ROLE_ADMIN);
        usuarioRepository.save(admin);

        log.info("Administrador inicial creado: {}", adminMail);
    }
}
