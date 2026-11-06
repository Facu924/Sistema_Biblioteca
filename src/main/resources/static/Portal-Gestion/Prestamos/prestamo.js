// Gestión de préstamos: conectada con la API (listado, filtros, nuevo préstamo y devolución).

document.addEventListener('DOMContentLoaded', async () => {
    // Menú lateral: colapsar / desplegar
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
    }

    // Solo personal de la biblioteca. El administrador consulta, pero no registra operaciones.
    const usuario = await Sesion.requerir(['ROLE_BIBLIOTECARIA', 'ROLE_ADMIN']);
    if (!usuario) return;
    const puedeOperar = usuario.tipoUsuario === 'ROLE_BIBLIOTECARIA';
    Ui.activarModales();

    const ESTADOS = {
        ACTIVO: { clase: 'active', icono: 'ph-clock', texto: 'Activo' },
        DEVUELTO: { clase: 'returned', icono: 'ph-check-circle', texto: 'Devuelto' },
        VENCIDO: { clase: 'overdue', icono: 'ph-warning-circle', texto: 'Vencido' }
    };
    const TIPOS = { AULA: 'Aula', SALA: 'Sala', DOMICILIO: 'Domicilio' };
    const TIPOS_LECTOR = { ALUMNO: 'Alumno', PROFESOR: 'Profesor', PERSONAL_ADMINISTRATIVO: 'Personal administrativo' };
    const COLORES = ['blue', 'light-blue', 'soft-blue'];
    const TAMANIO = 10;

    const el = id => document.getElementById(id);
    const tabla = el('tablaPrestamos');
    const filtro = { estado: '', texto: '', pagina: 0 };
    let prestamos = [];

    const plural = (n, uno, varios) => n + ' ' + (n === 1 ? uno : varios);

    // ------------------------------------------------------------- listado
    function estaVencido(p) {
        // Aunque la tarea diaria todavía no lo haya marcado, un préstamo abierto pasado de fecha está vencido
        return p.estadoPrestamo === 'VENCIDO' || (p.estadoPrestamo === 'ACTIVO' && p.diasAtraso > 0);
    }

    function filaPrestamo(p) {
        const vencido = estaVencido(p);
        const estado = ESTADOS[vencido ? 'VENCIDO' : p.estadoPrestamo];
        const abierto = p.estadoPrestamo !== 'DEVUELTO';
        const color = COLORES[p.idLector % COLORES.length];
        const inicial = Ui.esc((p.nombreLector || '?').trim().charAt(0).toUpperCase());
        const vence = vencido && abierto
            ? `<strong class="text-danger">${Ui.fecha(p.fechaLimiteEntrega)}</strong>`
            : Ui.fecha(p.fechaLimiteEntrega);

        let acciones;
        if (!abierto) {
            acciones = `<span class="info-text">Completado${p.diasAtraso > 0 ? ' (' + plural(p.diasAtraso, 'día', 'días') + ' de atraso)' : ''}</span>`;
        } else if (puedeOperar) {
            acciones = `<button class="btn-action ${vencido ? 'danger' : 'success'}" type="button" data-devolver="${p.idPrestamo}">
                            <i class="ph ${vencido ? 'ph-arrow-u-up-left' : 'ph-check-circle'}"></i> Devolver</button>`;
        } else {
            acciones = '<span class="info-text">Solo lectura</span>';
        }

        return `<tr>
            <td>
                <div class="user-cell">
                    <div class="user-avatar ${color}">${inicial}</div>
                    <div>
                        <span class="user-fullname">${Ui.esc(p.nombreLector)}</span>
                        <span class="isbn" style="display:block">Legajo ${Ui.esc(p.legajo)}</span>
                    </div>
                </div>
            </td>
            <td>
                <div class="book-info">
                    <strong>${Ui.esc(p.tituloLibro)}</strong>
                    <span class="isbn">${p.isbn ? Ui.esc(p.isbn) + ' · ' : ''}Inv. ${Ui.esc(p.numeroInventario)}</span>
                </div>
            </td>
            <td>${Ui.fecha(p.fechaPrestamo)}<span class="isbn" style="display:block">${TIPOS[p.tipoPrestamo] || ''}</span></td>
            <td>${vence}</td>
            <td>${Ui.fecha(p.fechaDevolucionReal)}</td>
            <td><span class="status-tag ${estado.clase}"><i class="ph ${estado.icono}"></i> ${estado.texto}</span></td>
            <td><div class="actions">${acciones}</div></td>
        </tr>`;
    }

    function pintarTabla() {
        tabla.innerHTML = prestamos.length
            ? prestamos.map(filaPrestamo).join('')
            : '<tr><td colspan="7" class="ui-vacio">No hay préstamos para mostrar.</td></tr>';
    }

    function pintarPaginacion(r) {
        const contenedor = el('paginacion');
        if (r.totalElementos === 0) { contenedor.innerHTML = ''; return; }
        const desde = r.pagina * r.tamanio + 1;
        const hasta = Math.min(desde + r.contenido.length - 1, r.totalElementos);
        const anterior = r.pagina > 0;
        const siguiente = r.pagina + 1 < r.totalPaginas;
        contenedor.innerHTML = `
            <span>Mostrando ${desde}–${hasta} de ${r.totalElementos}</span>
            <div class="botones">
                <button class="filter-btn" type="button" data-pagina="${r.pagina - 1}" ${anterior ? '' : 'disabled style="opacity:.5;cursor:not-allowed"'}>Anterior</button>
                <span style="align-self:center">Página ${r.pagina + 1} de ${r.totalPaginas}</span>
                <button class="filter-btn" type="button" data-pagina="${r.pagina + 1}" ${siguiente ? '' : 'disabled style="opacity:.5;cursor:not-allowed"'}>Siguiente</button>
            </div>`;
    }

    async function cargar() {
        try {
            const r = await Api.get('/api/prestamos', {
                estado: filtro.estado, texto: filtro.texto, pagina: filtro.pagina, tamanio: TAMANIO
            });
            prestamos = r.contenido;
            pintarTabla();
            pintarPaginacion(r);
            el('subtitulo').textContent = r.totalElementos === 1
                ? '1 préstamo encontrado' : r.totalElementos + ' préstamos encontrados';
        } catch (e) {
            Ui.toast(e.message, 'error');
        }
    }

    async function cargarResumen() {
        try {
            const r = await Api.get('/api/prestamos/resumen');
            el('kpiActivos').textContent = r.activos;
            el('kpiDevueltos').textContent = r.devueltos;
            el('kpiVencidos').textContent = r.vencidos;
            const badge = el('badgeMora');
            badge.textContent = r.vencidos;
            badge.hidden = r.vencidos === 0;
        } catch (e) { /* las tarjetas quedan en "–" */ }
    }

    // --------------------------------------------------- filtros y búsqueda
    const botonesEstado = document.querySelectorAll('.filter-btn[data-estado]');
    botonesEstado.forEach(btn => btn.addEventListener('click', () => {
        botonesEstado.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        filtro.estado = btn.dataset.estado;
        filtro.pagina = 0;
        cargar();
    }));

    el('buscar').addEventListener('input', Ui.debounce(() => {
        filtro.texto = el('buscar').value.trim();
        filtro.pagina = 0;
        cargar();
    }, 350));

    el('buscarNav').addEventListener('keydown', ev => {
        if (ev.key !== 'Enter') return;
        el('buscar').value = el('buscarNav').value;
        filtro.texto = el('buscar').value.trim();
        filtro.pagina = 0;
        cargar();
    });

    // La campana lleva directo a los vencidos
    document.querySelector('.notif-btn').addEventListener('click', () => {
        document.querySelector('.filter-btn[data-estado="VENCIDO"]').click();
    });

    el('paginacion').addEventListener('click', ev => {
        const btn = ev.target.closest('[data-pagina]');
        if (!btn || btn.disabled) return;
        filtro.pagina = Number(btn.dataset.pagina);
        cargar();
    });

    // ------------------------------------------------------ nuevo préstamo
    const pLegajo = el('pLegajo');
    const pInventario = el('pInventario');
    const pLector = el('pLector');
    const pError = el('pError');

    function mostrarError(caja, e) {
        const detalle = e.campos ? ' ' + Object.values(e.campos).join(' ') : '';
        caja.textContent = e.message + detalle;
        caja.hidden = false;
    }

    async function consultarLector() {
        const legajo = pLegajo.value.trim();
        pLector.hidden = true;
        if (!legajo) return;
        try {
            const l = await Api.get('/api/lectores', { legajo });
            const perfil = l.perfilInstitucional ? ' · ' + Ui.esc(l.perfilInstitucional) : '';
            let html = `<strong>${Ui.esc(l.nombre)}</strong> · ${TIPOS_LECTOR[l.tipoLector] || ''}${perfil}
                        <br>Materiales en préstamo: ${l.prestamosAbiertos} de ${l.limitePrestamos}`;
            if (!l.habilitado) html += `<br><strong>No se le puede prestar:</strong> ${Ui.esc(l.motivoNoHabilitado)}`;
            pLector.className = 'ui-aviso ' + (l.habilitado ? 'ok' : 'error');
            pLector.innerHTML = html;
        } catch (e) {
            pLector.className = 'ui-aviso error';
            pLector.textContent = e.message;
        }
        pLector.hidden = false;
    }

    el('btnNuevo').addEventListener('click', () => {
        el('formPrestamo').reset();
        pLector.hidden = true;
        pError.hidden = true;
        Ui.abrir('modalPrestamo');
        setTimeout(() => pLegajo.focus(), 50);
    });

    // Enter en el legajo consulta al lector y pasa al inventario, para trabajar sin el mouse
    pLegajo.addEventListener('keydown', async ev => {
        if (ev.key !== 'Enter') return;
        ev.preventDefault();
        await consultarLector();
        pInventario.focus();
    });
    pLegajo.addEventListener('blur', consultarLector);

    el('formPrestamo').addEventListener('submit', async ev => {
        ev.preventDefault();
        pError.hidden = true;
        const boton = el('pEnviar');
        boton.disabled = true;
        try {
            const r = await Api.post('/api/prestamos', {
                legajo: pLegajo.value.trim(),
                numeroInventario: pInventario.value.trim(),
                tipoPrestamo: el('pTipo').value
            });
            Ui.cerrar('modalPrestamo');
            Ui.toast(`Préstamo registrado: «${r.tituloLibro}» a ${r.nombreLector}. Vence el ${Ui.fecha(r.fechaLimiteEntrega)}.`, 'ok', 7000);
            cargar();
            cargarResumen();
        } catch (e) {
            mostrarError(pError, e); // regla de negocio incumplida: stock mínimo, límite, penalización, etc.
        } finally {
            boton.disabled = false;
        }
    });

    // ----------------------------------------------------------- devolución
    const dEstado = el('dEstado');
    const dObs = el('dObs');
    const dPena = el('dPena');
    const dError = el('dError');
    let aDevolver = null;

    function actualizarCamposDevolucion() {
        const atraso = aDevolver.diasAtraso || 0;
        const hayProblema = dEstado.value !== 'DEVUELTO_OK';
        el('dObsCampo').hidden = !hayProblema;

        if (!hayProblema && atraso === 0) {
            el('dPenaCampo').hidden = true;
            return;
        }
        el('dPenaCampo').hidden = false;
        const tocado = dPena.dataset.tocado === '1';

        if (!hayProblema) {
            if (!tocado) dPena.value = atraso;
            el('dPenaAyuda').textContent = `Sugerido: 1 día por cada día de atraso (${atraso}). Podés cambiar la cantidad, o poner 0 para no penalizar.`;
        } else {
            if (!tocado) dPena.value = '';
            el('dPenaAyuda').textContent = (atraso > 0 ? `Además tiene ${plural(atraso, 'día', 'días')} de atraso. ` : '')
                + 'Vacío: la penalización queda vigente hasta que la levantes. 0: no penalizar. Un número: esa cantidad de días.';
        }
    }

    function abrirDevolucion(p) {
        aDevolver = p;
        dEstado.value = 'DEVUELTO_OK';
        dObs.value = '';
        dPena.value = '';
        dPena.dataset.tocado = '0';
        dError.hidden = true;
        el('dResumen').innerHTML = `<strong>${Ui.esc(p.tituloLibro)}</strong> (Inv. ${Ui.esc(p.numeroInventario)})<br>
            Lector: ${Ui.esc(p.nombreLector)} · Legajo ${Ui.esc(p.legajo)}<br>
            Vencía el ${Ui.fecha(p.fechaLimiteEntrega)}${p.diasAtraso > 0
                ? ` · <strong style="color:#b91c1c">${plural(p.diasAtraso, 'día', 'días')} de atraso</strong>` : ''}`;
        actualizarCamposDevolucion();
        Ui.abrir('modalDevolucion');
    }

    tabla.addEventListener('click', ev => {
        const btn = ev.target.closest('[data-devolver]');
        if (!btn) return;
        const p = prestamos.find(x => String(x.idPrestamo) === btn.dataset.devolver);
        if (p) abrirDevolucion(p);
    });

    dEstado.addEventListener('change', actualizarCamposDevolucion);
    dPena.addEventListener('input', () => { dPena.dataset.tocado = '1'; });

    el('formDevolucion').addEventListener('submit', async ev => {
        ev.preventDefault();
        dError.hidden = true;
        const hayProblema = dEstado.value !== 'DEVUELTO_OK';
        if (hayProblema && !dObs.value.trim()) {
            dError.textContent = 'Indicá en las observaciones qué ocurrió con el ejemplar.';
            dError.hidden = false;
            return;
        }
        const boton = el('dEnviar');
        boton.disabled = true;
        try {
            const penaVisible = !el('dPenaCampo').hidden && dPena.value !== '';
            const r = await Api.post('/api/devoluciones', {
                numeroInventario: aDevolver.numeroInventario,
                estadoEntrega: dEstado.value,
                observaciones: dObs.value.trim() || null,
                diasPenalizacion: penaVisible ? Number(dPena.value) : null
            });
            Ui.cerrar('modalDevolucion');
            Ui.toast(`Devolución registrada: «${r.prestamo.tituloLibro}» de ${r.prestamo.nombreLector}.`, 'ok', 6000);
            if (r.penalizacion) {
                const hasta = r.penalizacion.fechaFin ? 'hasta el ' + Ui.fecha(r.penalizacion.fechaFin) : 'hasta que se levante';
                Ui.toast(`Penalización aplicada (${hasta}): ${r.penalizacion.motivo}`, 'aviso', 10000);
            }
            if (r.reservasPendientesDelLibro > 0) {
                Ui.toast(`Hay ${plural(r.reservasPendientesDelLibro, 'reserva pendiente', 'reservas pendientes')} de este título: avisá a quien sigue en la cola.`, 'info', 10000);
            }
            cargar();
            cargarResumen();
        } catch (e) {
            mostrarError(dError, e);
        } finally {
            boton.disabled = false;
        }
    });

    // ------------------------------------------- devolución con escáner
    const eInventario = el('eInventario');
    const eError = el('eError');

    function abrirEscaneo() {
        el('formEscaneo').reset();
        eError.hidden = true;
        Ui.abrir('modalEscaneo');
        setTimeout(() => eInventario.focus(), 50);
    }

    el('btnDevolverEscaner').addEventListener('click', abrirEscaneo);

    el('formEscaneo').addEventListener('submit', async ev => {
        ev.preventDefault();
        eError.hidden = true;
        try {
            const p = await Api.get('/api/prestamos/inventario/' + encodeURIComponent(eInventario.value.trim()));
            Ui.cerrar('modalEscaneo');
            abrirDevolucion(p);
        } catch (e) {
            mostrarError(eError, e); // por ejemplo: ese ejemplar no tiene un préstamo abierto
        }
    });

    // ------------------------------- accesos directos desde el dashboard
    const params = new URLSearchParams(window.location.search);
    const estadoInicial = params.get('estado');
    if (['ACTIVO', 'DEVUELTO', 'VENCIDO'].includes(estadoInicial)) {
        filtro.estado = estadoInicial;
        botonesEstado.forEach(b => b.classList.toggle('active', b.dataset.estado === estadoInicial));
    }

    cargar();
    cargarResumen();

    if (puedeOperar && params.get('nuevo') === '1') el('btnNuevo').click();
    if (puedeOperar && params.get('devolver') === '1') abrirEscaneo();
});
