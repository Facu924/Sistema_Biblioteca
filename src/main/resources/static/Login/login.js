// Pantalla de ingreso: conectada con Spring Security (sesión con cookie).

// Mostrar u ocultar la contraseña
function togglePassword() {
    const campo = document.getElementById('password');
    campo.type = campo.type === 'password' ? 'text' : 'password';
}

document.addEventListener('DOMContentLoaded', async () => {
    const formulario = document.getElementById('loginForm');
    const error = document.getElementById('loginError');
    const boton = document.getElementById('btnIngresar');

    function mostrarError(mensaje) {
        error.textContent = mensaje;
        error.hidden = false;
    }

    // Si ya hay una sesión iniciada, se salta el ingreso
    try {
        const u = await Sesion.cargar({ sinRedireccion: true });
        window.location.href = Sesion.destinoPorRol(u.tipoUsuario);
        return;
    } catch (e) { /* sin sesión: se muestra el formulario */ }

    // Números reales del catálogo en el panel izquierdo
    try {
        const r = await Api.get('/api/publico/resumen', null, { sinRedireccion: true });
        const formato = n => Number(n).toLocaleString('es-AR');
        document.getElementById('statLibros').textContent = formato(r.libros);
        document.getElementById('statDisponibles').textContent = formato(r.ejemplaresDisponibles);
        document.getElementById('statDigitales').textContent = formato(r.librosDigitales);
    } catch (e) { /* los números quedan en "—" */ }

    document.getElementById('olvido').addEventListener('click', ev => {
        ev.preventDefault();
        Ui.toast('Para blanquear tu contraseña acercate al mostrador de la biblioteca: la bibliotecaria o el administrador te asignan una nueva.', 'info', 8000);
    });

    formulario.addEventListener('submit', async ev => {
        ev.preventDefault();
        error.hidden = true;

        const mail = document.getElementById('mail').value.trim();
        const clave = document.getElementById('password').value;
        if (!mail || !clave) {
            mostrarError('Ingresá tu correo y tu contraseña.');
            return;
        }

        boton.disabled = true;
        boton.textContent = 'Ingresando...';
        try {
            await Api.login(mail, clave);
            const usuario = await Sesion.cargar();
            window.location.href = Sesion.destinoPorRol(usuario.tipoUsuario);
        } catch (e) {
            // El servidor distingue: credenciales inválidas, cuenta bloqueada o usuario inactivo
            mostrarError(e.message);
            boton.disabled = false;
            boton.textContent = 'Iniciar Sesión';
            document.getElementById('password').select();
        }
    });
});
