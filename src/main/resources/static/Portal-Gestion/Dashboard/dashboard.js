// Dashboard: indicadores, gráficos, actividad reciente y accesos rápidos, con datos reales de la API.

document.addEventListener('DOMContentLoaded', async () => {
    // Menú lateral: colapsar / desplegar
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
    }

    const usuario = await Sesion.requerir(['ROLE_BIBLIOTECARIA', 'ROLE_ADMIN']);
    if (!usuario) return;
    const esBibliotecaria = usuario.tipoUsuario === 'ROLE_BIBLIOTECARIA';
    Ui.activarModales();

    const el = id => document.getElementById(id);
    const num = n => Number(n).toLocaleString('es-AR');
    const plural = (n, uno, varios) => n + ' ' + (n === 1 ? uno : varios);

    // ------------------------------------------------------------ indicadores
    function pintarIndicadores(d) {
        el('kpiCatalogo').textContent = num(d.ejemplaresTotal);
        el('kpiCatalogoTrend').textContent = num(d.ejemplaresDisponibles) + ' dispon.';
        el('kpiCatalogo').closest('.kpi-card').title = num(d.librosEnCatalogo) + ' títulos · ejemplares físicos sin contar las bajas';

        el('kpiPrestamos').textContent = num(d.prestamosAbiertos);
        el('kpiPrestamosTrend').textContent = num(d.prestamosHoy) + ' hoy';

        if (d.tasaDevolucionATiempo === null || d.tasaDevolucionATiempo === undefined) {
            el('kpiTasa').textContent = '—';
            el('kpiTasaTrend').textContent = 'sin datos';
        } else {
            el('kpiTasa').textContent = Math.round(d.tasaDevolucionATiempo) + '%';
            el('kpiTasaTrend').textContent = d.tasaDevolucionATiempo.toFixed(1).replace('.', ',') + '% a tiempo';
        }

        el('kpiMora').textContent = num(d.lectoresMorosos + d.usuariosBloqueados);
        el('kpiMoraCard').title = plural(d.lectoresMorosos, 'lector en mora', 'lectores en mora')
            + ' · ' + plural(d.usuariosBloqueados, 'cuenta bloqueada', 'cuentas bloqueadas');

        const badge = el('badgeMora');
        badge.textContent = d.prestamosVencidos;
        badge.hidden = d.prestamosVencidos === 0;

        el('txtReservas').textContent = 'Reservas Pendientes (' + d.reservasPendientes + ')';
        el('txtDesbloqueos').textContent = 'Desbloquear Usuarios (' + d.usuariosBloqueados + ')';
    }

    async function refrescarIndicadores() {
        try { pintarIndicadores(await Api.get('/api/dashboard')); } catch (e) { /* quedan los anteriores */ }
    }

    // --------------------------------------------------------------- gráficos
    function sinDatos(idCanvas, mensaje) {
        const canvas = el(idCanvas);
        const aviso = document.createElement('div');
        aviso.className = 'ui-vacio';
        aviso.textContent = mensaje;
        canvas.replaceWith(aviso);
    }

    function pintarGraficos(d) {
        if (typeof Chart === 'undefined') {
            Ui.toast('No se pudieron cargar los gráficos: la librería se descarga de internet y no hay conexión.', 'aviso', 8000);
            return;
        }
        const ejes = { color: '#94a3b8', font: { size: 10 }, precision: 0 };

        // 1. Préstamos y devoluciones de los últimos 6 meses
        const flujo = d.flujoMensual;
        if (flujo.every(m => m.prestamos === 0 && m.devoluciones === 0)) {
            sinDatos('chartPrestamos', 'Todavía no hay movimientos para graficar.');
        } else {
            new Chart(el('chartPrestamos').getContext('2d'), {
                type: 'line',
                data: {
                    labels: flujo.map(m => m.etiqueta),
                    datasets: [
                        { label: 'Préstamos', data: flujo.map(m => m.prestamos), borderColor: '#2563eb',
                          backgroundColor: 'rgba(37, 99, 235, 0.08)', borderWidth: 2.5, fill: true, tension: 0.35 },
                        { label: 'Devoluciones', data: flujo.map(m => m.devoluciones), borderColor: '#10b981',
                          backgroundColor: 'transparent', borderWidth: 2.5, tension: 0.35 }
                    ]
                },
                options: {
                    responsive: true, maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 10 } } } },
                    scales: {
                        y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: ejes },
                        x: { grid: { display: false }, ticks: ejes }
                    }
                }
            });
            const anterior = flujo[flujo.length - 2].prestamos;
            const actual = flujo[flujo.length - 1].prestamos;
            if (anterior > 0) {
                const pct = Math.round((actual - anterior) * 100 / anterior);
                el('flujoBadge').textContent = (pct >= 0 ? '+' : '') + pct + '% vs mes anterior';
                el('flujoBadge').hidden = false;
            }
        }

        // 2. Los 5 libros más prestados
        if (d.topLibros.length === 0) {
            sinDatos('chartTopLibros', 'No hubo préstamos en los últimos 30 días.');
        } else {
            new Chart(el('chartTopLibros').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: d.topLibros.map(l => l.titulo.length > 24 ? l.titulo.substring(0, 23) + '…' : l.titulo),
                    datasets: [{ data: d.topLibros.map(l => l.cantidad), backgroundColor: '#38bdf8', borderRadius: 6 }]
                },
                options: {
                    indexAxis: 'y', responsive: true, maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                title: items => d.topLibros[items[0].dataIndex].titulo,
                                afterTitle: items => d.topLibros[items[0].dataIndex].autor
                            }
                        }
                    },
                    scales: {
                        x: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: ejes },
                        y: { grid: { display: false }, ticks: { color: '#475569', font: { size: 11 } } }
                    }
                }
            });
        }

        // 3. Títulos por categoría CDU
        if (d.librosPorCategoria.length === 0) {
            sinDatos('chartCategorias', 'Todavía no hay libros cargados.');
        } else {
            new Chart(el('chartCategorias').getContext('2d'), {
                type: 'doughnut',
                data: {
                    labels: d.librosPorCategoria.map(c => c.categoria),
                    datasets: [{
                        data: d.librosPorCategoria.map(c => c.cantidad),
                        backgroundColor: ['#2563eb', '#38bdf8', '#1e3a8a', '#93c5fd', '#60a5fa', '#cbd5e1']
                    }]
                },
                options: {
                    responsive: true, maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 10 }, padding: 8 } } }
                }
            });
        }
    }

    // ------------------------------------------------------ actividad reciente
    function hace(iso) {
        const minutos = Math.floor((Date.now() - new Date(String(iso).substring(0, 19)).getTime()) / 60000);
        if (minutos < 1) return 'recién';
        if (minutos < 60) return 'hace ' + minutos + ' min';
        const horas = Math.floor(minutos / 60);
        if (horas < 24) return 'hace ' + horas + ' h';
        const dias = Math.floor(horas / 24);
        return 'hace ' + plural(dias, 'día', 'días');
    }

    function pintarActividad(lista) {
        const ul = el('listaActividad');
        if (lista.length === 0) {
            ul.innerHTML = '<li class="ui-vacio">Todavía no hay movimientos en el mostrador.</li>';
            return;
        }
        ul.innerHTML = lista.map(a => {
            const esPrestamo = a.tipo === 'PRESTAMO';
            const iniciales = a.lector.split(' ').filter(Boolean).slice(0, 2).map(s => s[0]).join('').toUpperCase();
            return `<li class="activity-item">
                <div class="item-avatar ${esPrestamo ? 'blue' : 'light-blue'}">${Ui.esc(iniciales)}</div>
                <div class="item-info">
                    <h4>${Ui.esc(a.lector)}</h4>
                    <p>${Ui.esc(a.libro)}</p>
                </div>
                <div class="item-status">
                    <span class="status-tag ${esPrestamo ? 'prestamo' : 'devolucion'}">${esPrestamo ? 'Préstamo' : 'Devolución'}</span>
                    <span class="time-text">${hace(a.fecha)}</span>
                </div>
            </li>`;
        }).join('');
    }

    // ------------------------------------------------- reservas pendientes
    async function abrirReservas() {
        const cuerpo = el('cuerpoReservas');
        cuerpo.innerHTML = '<div class="ui-vacio">Cargando...</div>';
        Ui.abrir('modalReservas');
        try {
            const r = await Api.get('/api/reservas', { estado: 'PENDIENTE', tamanio: 50 });
            if (r.contenido.length === 0) {
                cuerpo.innerHTML = '<div class="ui-vacio">No hay reservas pendientes.</div>';
                return;
            }
            const filas = r.contenido.map(x => `<tr>
                <td><strong>${Ui.esc(x.tituloLibro)}</strong></td>
                <td>${Ui.esc(x.nombreLector)}<span class="ui-ayuda" style="display:block">Legajo ${Ui.esc(x.legajo)}</span></td>
                <td>${Ui.fecha(x.fechaSolicitud)}</td>
                <td>#${x.posicionEnCola}</td>
                <td>${esBibliotecaria ? `<button type="button" class="ui-btn secundario chico" data-cancelar="${x.idReserva}">Cancelar</button>` : ''}</td>
            </tr>`).join('');
            cuerpo.innerHTML = `<table class="ui-tabla">
                <thead><tr><th>Libro</th><th>Lector</th><th>Solicitada</th><th>En cola</th><th></th></tr></thead>
                <tbody>${filas}</tbody></table>`
                + (r.totalElementos > r.contenido.length
                    ? `<div class="ui-ayuda" style="margin-top:8px">Se muestran las primeras ${r.contenido.length} de ${r.totalElementos}.</div>` : '')
                + '<div class="ui-ayuda" style="margin-top:8px">Se atienden por orden de llegada: la que está en cola #1 es la próxima.</div>';
        } catch (e) {
            cuerpo.innerHTML = '<div class="ui-aviso error"></div>';
            cuerpo.firstChild.textContent = e.message;
        }
    }

    el('cuerpoReservas').addEventListener('click', async ev => {
        const boton = ev.target.closest('[data-cancelar]');
        if (!boton) return;
        boton.disabled = true;
        try {
            await Api.post('/api/reservas/' + boton.dataset.cancelar + '/cancelar');
            Ui.toast('Reserva cancelada.', 'ok');
            abrirReservas();
            refrescarIndicadores();
        } catch (e) {
            Ui.toast(e.message, 'error');
            boton.disabled = false;
        }
    });

    // ------------------------------------------------- usuarios bloqueados
    async function abrirBloqueados() {
        el('avisoBloqueados').innerHTML = '';
        await cargarBloqueados();
        Ui.abrir('modalBloqueados');
    }

    async function cargarBloqueados() {
        const lista = el('listaBloqueados');
        lista.innerHTML = '<div class="ui-vacio">Cargando...</div>';
        try {
            const r = await Api.get('/api/usuarios', { bloqueado: true, tamanio: 50 });
            if (r.contenido.length === 0) {
                lista.innerHTML = '<div class="ui-vacio">No hay cuentas bloqueadas.</div>';
                return;
            }
            const filas = r.contenido.map(u => `<tr>
                <td><strong>${Ui.esc(u.apellido + ', ' + u.nombre)}</strong><span class="ui-ayuda" style="display:block">${Ui.esc(u.mail)}</span></td>
                <td>${Ui.esc(u.legajo)}</td>
                <td>${u.intentosFallidos}</td>
                <td style="white-space:nowrap">
                    <button type="button" class="ui-btn secundario chico" data-desbloquear="${u.idUsuario}">Desbloquear</button>
                    <button type="button" class="ui-btn primario chico" data-desbloquear="${u.idUsuario}" data-clave="1" title="Además genera una contraseña temporal">Con clave nueva</button>
                </td>
            </tr>`).join('');
            lista.innerHTML = `<table class="ui-tabla">
                <thead><tr><th>Usuario</th><th>Legajo</th><th>Intentos</th><th></th></tr></thead>
                <tbody>${filas}</tbody></table>`;
        } catch (e) {
            lista.innerHTML = '<div class="ui-aviso error"></div>';
            lista.firstChild.textContent = e.message;
        }
    }

    el('listaBloqueados').addEventListener('click', async ev => {
        const boton = ev.target.closest('[data-desbloquear]');
        if (!boton) return;
        const conClave = boton.dataset.clave === '1';
        boton.disabled = true;
        try {
            const r = await Api.post('/api/usuarios/' + boton.dataset.desbloquear
                + '/desbloquear?claveTemporal=' + conClave);
            const aviso = el('avisoBloqueados');
            aviso.innerHTML = '';
            const caja = document.createElement('div');
            caja.className = 'ui-aviso ok';
            if (r && r.claveTemporal) {
                caja.innerHTML = 'Cuenta desbloqueada. Contraseña temporal: <strong>' + Ui.esc(r.claveTemporal)
                    + '</strong><br>Se muestra una sola vez: pasásela al usuario ahora.';
            } else {
                caja.textContent = 'Cuenta desbloqueada.';
            }
            aviso.appendChild(caja);
            await cargarBloqueados();
            refrescarIndicadores();
        } catch (e) {
            Ui.toast(e.message, 'error');
            boton.disabled = false;
        }
    });

    // ----------------------------------------------------- accesos y búsqueda
    el('btnNuevoPrestamo').addEventListener('click', () => { window.location.href = '../Prestamos/prestamo.html?nuevo=1'; });
    el('btnDevolucion').addEventListener('click', () => { window.location.href = '../Prestamos/prestamo.html?devolver=1'; });
    el('btnReservas').addEventListener('click', abrirReservas);
    el('btnDesbloqueos').addEventListener('click', abrirBloqueados);
    document.querySelector('.notif-btn').addEventListener('click', () => {
        window.location.href = '../Prestamos/prestamo.html?estado=VENCIDO';
    });
    el('buscarGlobal').addEventListener('keydown', ev => {
        if (ev.key !== 'Enter') return;
        const texto = el('buscarGlobal').value.trim();
        window.location.href = '../Catalogo-libro/catalogo.html' + (texto ? '?texto=' + encodeURIComponent(texto) : '');
    });

    // ------------------------------------------------------------------ carga
    try {
        const datos = await Api.get('/api/dashboard');
        pintarIndicadores(datos);
        pintarGraficos(datos);
        pintarActividad(datos.actividadReciente);
    } catch (e) {
        Ui.toast(e.message, 'error');
    }
});
