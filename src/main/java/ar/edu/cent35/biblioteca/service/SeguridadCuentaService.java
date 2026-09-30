package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.reportes.AuditoriaSistema;
import ar.edu.cent35.biblioteca.repository.AuditoriaSistemaRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/** Intentos fallidos, bloqueo automático (HU-11) y desbloqueo por el administrador (HU-03). */
@Service
public class SeguridadCuentaService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaSistemaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final int maxIntentos;

    public SeguridadCuentaService(UsuarioRepository usuarioRepository,
                                  AuditoriaSistemaRepository auditoriaRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.security.max-intentos-fallidos:5}") int maxIntentos) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
        this.maxIntentos = maxIntentos;
    }

    @Transactional
    public void registrarIntentoFallido(String mail) {
        usuarioRepository.findByMail(mail).ifPresent(usuario -> {
            if (Boolean.TRUE.equals(usuario.getCuentaBloqueada())) {
                return;
            }
            int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= maxIntentos) {
                usuario.setCuentaBloqueada(true);
                auditar(null, "BLOQUEO_AUTOMATICO",
                        "Cuenta bloqueada tras " + intentos + " intentos fallidos: " + mail, null);
            }
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void registrarLoginExitoso(String mail) {
        usuarioRepository.findByMail(mail).ifPresent(usuario -> {
            if (usuario.getIntentosFallidos() != null && usuario.getIntentosFallidos() != 0) {
                usuario.setIntentosFallidos(0);
                usuarioRepository.save(usuario);
            }
        });
    }

    /**
     * Limpia el contador y reactiva el acceso. Si se pide, blanquea la contraseña
     * con una temporal y la devuelve (única vez que se puede ver); si no, devuelve null.
     */
    @Transactional
    public String desbloquear(Long idUsuario, boolean generarClaveTemporal, String adminMail, String ip) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));

        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueada(false);

        String claveTemporal = null;
        if (generarClaveTemporal) {
            claveTemporal = generarClave(12);
            usuario.setContrasena(passwordEncoder.encode(claveTemporal));
        }
        usuarioRepository.save(usuario);

        Usuario admin = usuarioRepository.findByMail(adminMail).orElse(null);
        auditar(admin, "DESBLOQUEO_CUENTA",
                "Desbloqueo de " + usuario.getMail() + (generarClaveTemporal ? " con clave temporal" : ""), ip);
        return claveTemporal;
    }

    private void auditar(Usuario actor, String accion, String detalles, String ip) {
        AuditoriaSistema a = new AuditoriaSistema();
        a.setUsuario(actor); // null = evento automático del sistema
        a.setAccion(accion);
        a.setDetalles(detalles);
        a.setFechaHora(LocalDateTime.now());
        a.setIpOrigen(ip);
        auditoriaRepository.save(a);
    }

    private String generarClave(int largo) {
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
