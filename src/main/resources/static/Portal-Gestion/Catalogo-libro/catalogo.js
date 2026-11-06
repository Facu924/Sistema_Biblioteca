// Catálogo de libros: listado, búsqueda, alta y edición, ejemplares y material digital.

document.addEventListener('DOMContentLoaded', async () => {
    // Menú lateral: colapsar / desplegar
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
    }

    // Personal de la biblioteca. La bibliotecaria edita; el administrador consulta.
    const usuario = await Sesion.requerir(['ROLE_BIBLIOTECARIA', 'ROLE_ADMIN']);
    if (!usuario) return;
    const puedeEditar = usuario.tipoUsuario === 'ROLE_BIBLIOTECARIA';
    Ui.activarModales();
    Ui.campana('../Prestamos/prestamo.html');

    const el = id => document.getElementById(id);
    const TAMANIO = 10;
    const COLORES_TAPA = ['cyan', 'green', 'orange', 'purple', 'dark-purple', 'yellow', 'light-blue'];
    const COLORES_CATEGORIA = ['blue', 'light-blue', 'soft-blue', 'soft-purple', 'soft-cyan'];
    const ESTADOS_EJEMPLAR = {
        DISPONIBLE: { clase: 'ok', texto: 'Disponible' },
        PRESTADO: { clase: 'aviso', texto: 'Prestado' },
        DANADO: { clase: 'error', texto: 'Dañado' },
        EXTRAVIADO: { clase: 'error', texto: 'Extraviado' },
        BAJA_LOGICA: { clase: 'neutro', texto: 'Baja' }
    };

    const filtro = { texto: '', idMateria: '', pagina: 0 };
    let libros = [];
    let materias = [];

    const plural = (n, uno, varios) => n + ' ' + (n === 1 ? uno : varios);
    // Compara nombres sin distinguir mayúsculas ni acentos, como lo hace la base de datos
    const normalizar = s => String(s).normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();

    function mostrarError(caja, e) {
        const detalle = e.campos ? ' ' + Object.values(e.campos).join(' ') : '';
        caja.textContent = e.message + detalle;
        caja.hidden = false;
    }

    // ===================================================== listado de libros
    function estadoLibro(l) {
        if (l.ejemplaresTotal === 0) {
            return l.esDigital ? { clase: 'success', texto: 'Digital' } : { clase: 'neutro', texto: 'Sin ejemplares' };
        }
        return l.ejemplaresDisponibles > 0
            ? { clase: 'success', texto: 'Disponible' }
            : { clase: 'danger', texto: 'Agotado' };
    }

    function filaLibro(l) {
        const estado = estadoLibro(l);
        const titulo = l.titulo.length > 36 ? l.titulo.substring(0, 35) + '…' : l.titulo;
        const tapa = COLORES_TAPA[l.idLibro % COLORES_TAPA.length];
        const categoria = COLORES_CATEGORIA[l.idMateria % COLORES_CATEGORIA.length];
        const disponibles = l.ejemplaresTotal === 0
            ? '–' : l.ejemplaresDisponibles + '/' + l.ejemplaresTotal;
        const acciones = `<button class="icon-btn" type="button" title="Ver detalle" data-ver="${l.idLibro}"><i class="ph ph-eye"></i></button>`
            + (puedeEditar
                ? `<button class="icon-btn" type="button" title="Editar" data-editar="${l.idLibro}"><i class="ph ph-pencil-simple"></i></button>
                   <button class="icon-btn danger" type="button" title="Eliminar" data-eliminar="${l.idLibro}"><i class="ph ph-trash"></i></button>`
                : '');
        return `<tr>
            <td class="text-muted">${Ui.esc(l.isbn || '—')}</td>
            <td>
                <div class="book-title-cell">
                    <span class="book-cover ${tapa}"><i class="ph ${l.esDigital ? 'ph-file-pdf' : 'ph-book-open'}"></i></span>
                    <strong title="${Ui.esc(l.titulo)}">${Ui.esc(titulo)}</strong>
                </div>
            </td>
            <td>${Ui.esc(l.nombreAutor)}</td>
            <td><span class="cat-pill ${categoria}" title="CDU ${Ui.esc(l.codigoCdu)}">${Ui.esc(l.nombreTema)}</span></td>
            <td>${Ui.esc(l.nombreEditorial)}</td>
            <td>${disponibles}${l.esDigital && l.ejemplaresTotal > 0 ? ' <i class="ph ph-file-pdf" title="Tiene archivo digital"></i>' : ''}</td>
            <td><span class="status-pill ${estado.clase}">${estado.texto}</span></td>
            <td><div class="actions">${acciones}</div></td>
        </tr>`;
    }

    function pintarPaginacion(r) {
        const cont = el('paginacion');
        if (r.totalElementos === 0) { cont.innerHTML = ''; return; }
        const desde = r.pagina * r.tamanio + 1;
        const hasta = Math.min(desde + r.contenido.length - 1, r.totalElementos);
        const anterior = r.pagina > 0;
        const siguiente = r.pagina + 1 < r.totalPaginas;
        cont.innerHTML = `
            <span>Mostrando ${desde}–${hasta} de ${r.totalElementos}</span>
            <div class="botones">
                <button class="filter-btn" type="button" data-pagina="${r.pagina - 1}" ${anterior ? '' : 'disabled style="opacity:.5;cursor:not-allowed"'}>Anterior</button>
                <span style="align-self:center">Página ${r.pagina + 1} de ${r.totalPaginas}</span>
                <button class="filter-btn" type="button" data-pagina="${r.pagina + 1}" ${siguiente ? '' : 'disabled style="opacity:.5;cursor:not-allowed"'}>Siguiente</button>
            </div>`;
    }

    async function cargar() {
        try {
            const r = await Api.get('/api/libros', {
                texto: filtro.texto, idMateria: filtro.idMateria, pagina: filtro.pagina, tamanio: TAMANIO
            });
            libros = r.contenido;
            el('tablaLibros').innerHTML = libros.length
                ? libros.map(filaLibro).join('')
                : '<tr><td colspan="8" class="ui-vacio">No hay libros para mostrar.</td></tr>';
            pintarPaginacion(r);
            el('subtitulo').textContent = r.totalElementos === 1
                ? '1 libro encontrado' : r.totalElementos + ' libros encontrados';
        } catch (e) {
            Ui.toast(e.message, 'error');
        }
    }

    // ============================================ filtros por categoría (CDU)
    function pintarFiltros() {
        const cont = el('filtrosCategoria');
        const actual = String(filtro.idMateria);
        if (materias.length <= 8) {
            cont.innerHTML = `<button class="filter-btn ${actual === '' ? 'active' : ''}" type="button" data-materia="">Todas</button>`
                + materias.map(m => `<button class="filter-btn ${actual === String(m.idMateria) ? 'active' : ''}" type="button"
                    data-materia="${m.idMateria}" title="CDU ${Ui.esc(m.codigoCdu)}">${Ui.esc(m.nombreTema)}</button>`).join('');
        } else {
            // Con muchas materias, un desplegable es más cómodo que una fila de botones
            cont.innerHTML = `<select class="filter-btn" id="selCategoria"><option value="">Todas las categorías</option>`
                + materias.map(m => `<option value="${m.idMateria}" ${actual === String(m.idMateria) ? 'selected' : ''}>${Ui.esc(m.codigoCdu + ' · ' + m.nombreTema)}</option>`).join('')
                + '</select>';
        }
    }

    function pintarSelectMaterias(seleccion) {
        el('lMateria').innerHTML = '<option value="">Elegí una materia…</option>'
            + materias.map(m => `<option value="${m.idMateria}">${Ui.esc(m.codigoCdu + ' · ' + m.nombreTema)}</option>`).join('')
            + (puedeEditar ? '<option value="__nueva__">＋ Nueva materia…</option>' : '');
        if (seleccion !== undefined) el('lMateria').value = String(seleccion);
    }

    async function cargarMaterias() {
        try {
            materias = (await Api.get('/api/materias', { tamanio: 100 })).contenido;
        } catch (e) {
            materias = [];
        }
        pintarFiltros();
        pintarSelectMaterias();
    }

    el('filtrosCategoria').addEventListener('click', ev => {
        const boton = ev.target.closest('[data-materia]');
        if (!boton) return;
        filtro.idMateria = boton.dataset.materia;
        filtro.pagina = 0;
        pintarFiltros();
        cargar();
    });
    el('filtrosCategoria').addEventListener('change', ev => {
        if (ev.target.id !== 'selCategoria') return;
        filtro.idMateria = ev.target.value;
        filtro.pagina = 0;
        cargar();
    });

    // ======================================================= búsqueda y tabla
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
    el('paginacion').addEventListener('click', ev => {
        const boton = ev.target.closest('[data-pagina]');
        if (!boton || boton.disabled) return;
        filtro.pagina = Number(boton.dataset.pagina);
        cargar();
    });

    el('tablaLibros').addEventListener('click', ev => {
        const ver = ev.target.closest('[data-ver]');
        const editar = ev.target.closest('[data-editar]');
        const eliminar = ev.target.closest('[data-eliminar]');
        if (ver) abrirDetalle(Number(ver.dataset.ver));
        if (editar) abrirEditar(Number(editar.dataset.editar));
        if (eliminar) {
            const libro = libros.find(l => String(l.idLibro) === eliminar.dataset.eliminar);
            if (libro) abrirEliminar(libro);
        }
    });

    // ================================================ alta y edición de libros
    let editandoId = null;
    const lError = el('lError');

    // Sugerencias mientras se escribe el autor o la editorial
    function sugerencias(idInput, idLista, url, campo) {
        el(idInput).addEventListener('input', Ui.debounce(async () => {
            const texto = el(idInput).value.trim();
            if (texto.length < 2) return;
            try {
                const r = await Api.get(url, { texto, tamanio: 10 });
                el(idLista).innerHTML = r.contenido.map(x => `<option value="${Ui.esc(x[campo])}"></option>`).join('');
            } catch (e) { /* sin sugerencias */ }
        }, 250));
    }
    sugerencias('lAutor', 'dlAutores', '/api/autores', 'nombreAutor');
    sugerencias('lEditorial', 'dlEditoriales', '/api/editoriales', 'nombreEditorial');

    el('lMateria').addEventListener('change', () => {
        el('lMateriaNueva').hidden = el('lMateria').value !== '__nueva__';
    });

    /** Busca por nombre; si no existe, lo crea. Devuelve el id. */
    async function resolverPorNombre(nombre, urlLista, campoNombre, campoId, urlAlta, cuerpoAlta) {
        const buscado = nombre.trim();
        const r = await Api.get(urlLista, { texto: buscado, tamanio: 20 });
        const igual = r.contenido.find(x => normalizar(x[campoNombre]) === normalizar(buscado));
        if (igual) return igual[campoId];
        const nuevo = await Api.post(urlAlta, cuerpoAlta(buscado));
        return nuevo[campoId];
    }

    function prepararFormulario(titulo) {
        el('formLibro').reset();
        el('tituloModalLibro').textContent = titulo;
        el('lMateriaNueva').hidden = true;
        lError.hidden = true;
    }

    function abrirNuevo() {
        editandoId = null;
        prepararFormulario('Agregar libro');
        Ui.abrir('modalLibro');
        setTimeout(() => el('lTitulo').focus(), 50);
    }

    async function abrirEditar(id) {
        try {
            const l = await Api.get('/api/libros/' + id);
            editandoId = id;
            prepararFormulario('Editar libro');
            el('lTitulo').value = l.titulo;
            el('lIsbn').value = l.isbn || '';
            el('lAutor').value = l.nombreAutor;
            el('lEditorial').value = l.nombreEditorial;
            el('lMateria').value = String(l.idMateria);
            el('lAnio').value = l.anioPublicacion ?? '';
            el('lPaginas').value = l.numeroPaginas ?? '';
            el('lEdicion').value = l.edicion || '';
            el('lResumen').value = l.resumen || '';
            Ui.abrir('modalLibro');
        } catch (e) {
            Ui.toast(e.message, 'error');
        }
    }

    if (puedeEditar) {
        el('btnAgregar').addEventListener('click', abrirNuevo);
    }

    el('formLibro').addEventListener('submit', async ev => {
        ev.preventDefault();
        lError.hidden = true;
        const boton = el('lEnviar');
        const materiaNueva = el('lMateria').value === '__nueva__';
        if (materiaNueva && (!el('lMatCodigo').value.trim() || !el('lMatNombre').value.trim())) {
            lError.textContent = 'Completá el código y el nombre de la nueva materia.';
            lError.hidden = false;
            return;
        }
        boton.disabled = true;
        try {
            const idAutor = await resolverPorNombre(el('lAutor').value, '/api/autores', 'nombreAutor', 'idAutor',
                '/api/autores', nombre => ({ nombreAutor: nombre }));
            const idEditorial = await resolverPorNombre(el('lEditorial').value, '/api/editoriales', 'nombreEditorial', 'idEditorial',
                '/api/editoriales', nombre => ({ nombreEditorial: nombre }));

            let idMateria;
            if (materiaNueva) {
                const m = await Api.post('/api/materias', {
                    codigoCdu: el('lMatCodigo').value.trim(), nombreTema: el('lMatNombre').value.trim()
                });
                idMateria = m.idMateria;
                await cargarMaterias();
                pintarSelectMaterias(idMateria);
            } else {
                idMateria = Number(el('lMateria').value);
            }

            const cuerpo = {
                isbn: el('lIsbn').value.trim() || null,
                titulo: el('lTitulo').value.trim(),
                idAutor, idEditorial, idMateria,
                anioPublicacion: el('lAnio').value ? Number(el('lAnio').value) : null,
                numeroPaginas: el('lPaginas').value ? Number(el('lPaginas').value) : null,
                edicion: el('lEdicion').value.trim() || null,
                resumen: el('lResumen').value.trim() || null
            };
            const nuevo = editandoId === null;
            const libro = nuevo ? await Api.post('/api/libros', cuerpo) : await Api.put('/api/libros/' + editandoId, cuerpo);

            Ui.cerrar('modalLibro');
            Ui.toast(nuevo ? 'Libro agregado: «' + libro.titulo + '».' : 'Cambios guardados.', 'ok');
            cargar();
            if (nuevo) abrirDetalle(libro.idLibro); // para cargar enseguida los ejemplares
        } catch (e) {
            mostrarError(lError, e);
        } finally {
            boton.disabled = false;
        }
    });

    // ====================================================== eliminar un libro
    let aEliminar = null;
    const eliminarError = el('eliminarError');

    function abrirEliminar(libro) {
        aEliminar = libro;
        el('eliminarTexto').innerHTML = `¿Querés eliminar «<strong>${Ui.esc(libro.titulo)}</strong>» del catálogo?<br>
            <span class="ui-ayuda">Solo se puede eliminar un libro que no tenga ejemplares. Si los tiene, primero hay que darlos de baja.</span>`;
        eliminarError.hidden = true;
        Ui.abrir('modalEliminar');
    }

    el('btnConfirmarEliminar').addEventListener('click', async () => {
        eliminarError.hidden = true;
        try {
            await Api.eliminar('/api/libros/' + aEliminar.idLibro);
            Ui.cerrar('modalEliminar');
            Ui.toast('Libro eliminado del catálogo.', 'ok');
            cargar();
        } catch (e) {
            mostrarError(eliminarError, e);
        }
    });

    // ==================================== detalle: ejemplares y material digital
    let detalleId = null;
    let ejemplaresActuales = [];

    function htmlDetalle(l, ejemplares) {
        const dato = (titulo, valor) => `<div><span>${titulo}</span><strong>${Ui.esc(valor || '—')}</strong></div>`;

        const filas = ejemplares.map(e => {
            const est = ESTADOS_EJEMPLAR[e.estadoEjemplar] || { clase: 'neutro', texto: e.estadoEjemplar };
            const prestado = e.estadoEjemplar === 'PRESTADO';
            return `<tr>
                <td><strong>${Ui.esc(e.numeroInventario)}</strong></td>
                <td>${Ui.esc(e.ubicacionEstante || '—')}</td>
                <td><span class="ui-etiqueta ${est.clase}">${est.texto}</span></td>
                ${puedeEditar ? `<td><button type="button" class="ui-btn secundario chico" data-accion="estado" data-id="${e.idEjemplar}"
                    ${prestado ? 'disabled title="Está prestado: primero registrá la devolución"' : ''}>Cambiar estado</button></td>` : ''}
            </tr>`;
        }).join('');

        const tablaEjemplares = ejemplares.length
            ? `<table class="ui-tabla"><thead><tr><th>Inventario</th><th>Ubicación</th><th>Estado</th>${puedeEditar ? '<th></th>' : ''}</tr></thead><tbody>${filas}</tbody></table>`
            : '<div class="ui-ayuda">Este libro todavía no tiene ejemplares físicos.</div>';

        const formEjemplar = puedeEditar ? `
            <div class="ui-fila-formulario">
                <div class="ui-campo" style="margin:0"><label for="nInv">Nuevo ejemplar: inventario</label>
                    <input type="text" id="nInv" maxlength="50" placeholder="Escaneá o escribí el número"></div>
                <div class="ui-campo" style="margin:0"><label for="nUbic">Ubicación</label>
                    <input type="text" id="nUbic" maxlength="100" placeholder="Ej: Estante A-3"></div>
                <button type="button" class="ui-btn primario" data-accion="agregar-ejemplar">Agregar</button>
            </div>` : '';

        const enlaces = l.esDigital
            ? `<p class="ui-enlaces"><a href="/api/libros/${l.idLibro}/archivo" target="_blank" rel="noopener">Ver en el navegador</a>
               <a href="/api/libros/${l.idLibro}/archivo?descargar=true">Descargar</a></p>`
            : '<p class="ui-ayuda">Este libro no tiene archivo digital.</p>';

        const formArchivo = puedeEditar ? `
            <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
                <input type="file" id="archivoDigital" accept=".pdf,.epub,application/pdf,application/epub+zip">
                <button type="button" class="ui-btn primario chico" data-accion="subir-archivo">${l.esDigital ? 'Reemplazar archivo' : 'Subir archivo'}</button>
                ${l.esDigital ? '<button type="button" class="ui-btn peligro chico" data-accion="quitar-archivo">Quitar</button>' : ''}
            </div>
            <div class="ui-ayuda">PDF o EPUB, hasta 50 MB.</div>` : '';

        return `
            <h4 class="ui-seccion">Datos</h4>
            <div class="ui-datos">
                ${dato('Autor', l.nombreAutor)}${dato('Editorial', l.nombreEditorial)}
                ${dato('Materia', l.codigoCdu + ' · ' + l.nombreTema)}${dato('ISBN', l.isbn)}
                ${dato('Año', l.anioPublicacion)}${dato('Páginas', l.numeroPaginas)}
                ${dato('Edición', l.edicion)}
            </div>
            ${l.resumen ? `<p class="ui-ayuda" style="margin-top:10px">${Ui.esc(l.resumen)}</p>` : ''}

            <h4 class="ui-seccion">Stock</h4>
            <p>${plural(l.ejemplaresDisponibles, 'ejemplar disponible', 'ejemplares disponibles')} de ${l.ejemplaresTotal}.
               Préstamo a domicilio:
               <span class="ui-etiqueta ${l.prestamoDomicilioHabilitado ? 'ok' : 'aviso'}">${l.prestamoDomicilioHabilitado ? 'habilitado' : 'no habilitado'}</span>
               ${l.ejemplaresDisponibles === 1 ? '<span class="ui-ayuda">Queda uno solo: se reserva para consulta en sala.</span>' : ''}</p>

            <h4 class="ui-seccion">Ejemplares</h4>
            ${tablaEjemplares}
            ${formEjemplar}
            <div id="detalleError" class="ui-aviso error" role="alert" hidden></div>

            <h4 class="ui-seccion">Material digital</h4>
            ${enlaces}
            ${formArchivo}`;
    }

    async function recargarDetalle() {
        try {
            const [libro, ejemplares] = await Promise.all([
                Api.get('/api/libros/' + detalleId),
                Api.get('/api/libros/' + detalleId + '/ejemplares')
            ]);
            ejemplaresActuales = ejemplares;
            el('detalleTitulo').textContent = libro.titulo;
            el('detalleCuerpo').innerHTML = htmlDetalle(libro, ejemplares);
        } catch (e) {
            el('detalleCuerpo').innerHTML = '<div class="ui-aviso error"></div>';
            el('detalleCuerpo').firstChild.textContent = e.message;
        }
    }

    async function abrirDetalle(id) {
        detalleId = id;
        el('detalleTitulo').textContent = 'Detalle del libro';
        el('detalleCuerpo').innerHTML = '<div class="ui-vacio">Cargando...</div>';
        Ui.abrir('modalDetalle');
        await recargarDetalle();
    }

    function errorDetalle(e) {
        const caja = el('detalleError');
        if (caja) mostrarError(caja, e); else Ui.toast(e.message, 'error');
    }

    async function agregarEjemplar() {
        const inventario = el('nInv').value.trim();
        if (!inventario) throw new Api.ApiError(0, 'Ingresá el número de inventario.');
        await Api.post('/api/libros/' + detalleId + '/ejemplares', {
            numeroInventario: inventario,
            ubicacionEstante: el('nUbic').value.trim() || null
        });
        Ui.toast('Ejemplar ' + inventario + ' agregado.', 'ok');
        const ubicacion = el('nUbic').value;
        await recargarDetalle();
        el('nUbic').value = ubicacion;   // se conserva para cargar varios seguidos en el mismo estante
        el('nInv').focus();
        cargar();
    }

    async function subirArchivo() {
        const archivo = el('archivoDigital').files[0];
        if (!archivo) throw new Api.ApiError(0, 'Elegí un archivo PDF o EPUB.');
        const datos = new FormData();
        datos.append('archivo', archivo);
        await Api.subir('/api/libros/' + detalleId + '/archivo', datos);
        Ui.toast('Archivo digital guardado.', 'ok');
        await recargarDetalle();
        cargar();
    }

    async function quitarArchivo() {
        if (!window.confirm('¿Quitar el archivo digital de este libro?')) return;
        await Api.eliminar('/api/libros/' + detalleId + '/archivo');
        Ui.toast('Archivo digital quitado.', 'ok');
        await recargarDetalle();
        cargar();
    }

    el('detalleCuerpo').addEventListener('click', async ev => {
        const boton = ev.target.closest('[data-accion]');
        if (!boton) return;
        try {
            switch (boton.dataset.accion) {
                case 'agregar-ejemplar': await agregarEjemplar(); break;
                case 'estado': abrirEstado(Number(boton.dataset.id)); break;
                case 'subir-archivo': await subirArchivo(); break;
                case 'quitar-archivo': await quitarArchivo(); break;
            }
        } catch (e) {
            errorDetalle(e);
        }
    });

    // El lector de códigos de barras confirma con Enter: agrega el ejemplar sin usar el mouse
    el('detalleCuerpo').addEventListener('keydown', async ev => {
        if (ev.key !== 'Enter' || ev.target.id !== 'nInv') return;
        ev.preventDefault();
        try { await agregarEjemplar(); } catch (e) { errorDetalle(e); }
    });

    // ================================================ cambio de estado de un ejemplar
    let ejemplarEnCambio = null;
    const eEstadoError = el('eEstadoError');
    const TEXTOS_CAMBIO = {
        DISPONIBLE: 'Disponible (vuelve a la estantería)',
        DANADO: 'Dañado',
        EXTRAVIADO: 'Extraviado',
        BAJA_LOGICA: 'Baja por expurgo (donación o reciclaje)'
    };

    function actualizarMotivo() {
        el('eObsCampo').hidden = el('eEstadoNuevo').value === 'DISPONIBLE';
    }

    function abrirEstado(idEjemplar) {
        const e = ejemplaresActuales.find(x => x.idEjemplar === idEjemplar);
        if (!e) return;
        ejemplarEnCambio = e;
        el('estadoResumen').innerHTML = `<strong>${Ui.esc(e.tituloLibro)}</strong><br>
            Inventario ${Ui.esc(e.numeroInventario)} · estado actual: ${(ESTADOS_EJEMPLAR[e.estadoEjemplar] || {}).texto || e.estadoEjemplar}`;
        el('eEstadoNuevo').innerHTML = Object.keys(TEXTOS_CAMBIO)
            .filter(clave => clave !== e.estadoEjemplar)
            .map(clave => `<option value="${clave}">${TEXTOS_CAMBIO[clave]}</option>`).join('');
        el('eObs').value = '';
        eEstadoError.hidden = true;
        actualizarMotivo();
        Ui.abrir('modalEstado');
    }

    el('eEstadoNuevo').addEventListener('change', actualizarMotivo);

    el('formEstado').addEventListener('submit', async ev => {
        ev.preventDefault();
        eEstadoError.hidden = true;
        const estado = el('eEstadoNuevo').value;
        const motivo = el('eObs').value.trim();
        if (estado !== 'DISPONIBLE' && !motivo) {
            eEstadoError.textContent = 'Indicá el motivo del cambio.';
            eEstadoError.hidden = false;
            return;
        }
        const boton = el('eEstadoEnviar');
        boton.disabled = true;
        try {
            await Api.post('/api/ejemplares/' + ejemplarEnCambio.idEjemplar + '/estado', {
                estado, observaciones: motivo || null
            });
            Ui.cerrar('modalEstado');
            Ui.toast('Estado actualizado.', 'ok');
            await recargarDetalle();
            cargar();
        } catch (e) {
            mostrarError(eEstadoError, e);
        } finally {
            boton.disabled = false;
        }
    });

    // =================================================================== inicio
    // El dashboard puede enviar una búsqueda en la dirección: catalogo.html?texto=...
    const inicial = new URLSearchParams(window.location.search).get('texto');
    if (inicial) {
        filtro.texto = inicial.trim();
        el('buscar').value = filtro.texto;
    }

    cargarMaterias();
    cargar();
});
