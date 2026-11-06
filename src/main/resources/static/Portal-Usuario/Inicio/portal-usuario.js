function filtrarCatalogo(tipo) {
    // 1. Obtener todas las tarjetas de libros y los botones de filtro
    const tarjetas = document.querySelectorAll('.book-card');
    const botones = document.querySelectorAll('.filter-btn');

    // 2. Actualizar la clase 'active' en los botones
    botones.forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    // 3. Mostrar u ocultar las tarjetas según el filtro seleccionado
    tarjetas.forEach(tarjeta => {
        const tipoTarjeta = tarjeta.getAttribute('data-tipo');
        
        if (tipo === 'todos' || tipoTarjeta === tipo) {
            tarjeta.style.display = 'flex'; // O 'block' según tu diseño original
        } else {
            tarjeta.style.display = 'none'; // Oculta las que no coinciden
        }
    });
}

// "Identificarse" lleva a la pantalla de ingreso
function openLoginModal() {
    window.location.href = '../../Login/login.html';
}

// Si ya hay una sesión iniciada, el botón pasa a ser "Salir"
document.addEventListener('DOMContentLoaded', async () => {
    const boton = document.querySelector('.btn-identificarse');
    if (!boton) return;
    try {
        const u = await Sesion.cargar({ sinRedireccion: true });
        boton.innerHTML = '<span class="user-icon"><i class="ph ph-sign-out"></i></span> Salir (' + Ui.esc(u.nombre) + ')';
        boton.onclick = () => Sesion.cerrar();
    } catch (e) { /* visitante sin sesión: queda "Identificarse" */ }
});
