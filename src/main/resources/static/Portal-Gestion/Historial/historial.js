// Historial conectado a la API existente; no requiere cambios en la base de datos.
document.addEventListener('DOMContentLoaded', async () => {
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggleBtn');
    if (toggleBtn && sidebar) toggleBtn.addEventListener('click', () => sidebar.classList.toggle('collapsed'));

    const usuario = await Sesion.requerir(['ROLE_BIBLIOTECARIA', 'ROLE_ADMIN']);
    if (!usuario) return;
    Ui.campana('../Prestamos/prestamo.html');

    const lista = document.getElementById('listaHistorial');
    const buscador = document.getElementById('buscarHistorial');
    const paginacion = document.getElementById('paginacionHistorial');
    const conteo = document.getElementById('conteoEventos');
    const TAMANIO_API = 100;
    const TAMANIO_VISTA = 12;
    let eventos = [];
    let tipo = 'TODOS';
    let paginaVista = 0;

    const primero = (obj, ...claves) => {
        for (const clave of claves) {
            const valor = obj?.[clave];
            if (valor !== undefined && valor !== null && valor !== '') return valor;
        }
        return null;
    };
    const texto = (obj, ...claves) => String(primero(obj, ...claves) ?? '');
    const mostrar = v => Ui.esc(String(v === null || v === undefined || v === '' ? '—' : v));
    const fecha = valor => valor ? Ui.fecha(valor) : '—';
    const detallesLector = v => texto(v, 'nombreLector', 'lectorNombre') ||
        [texto(v,'nombre','nombrePersona'), texto(v,'apellido','apellidoPersona')].filter(Boolean).join(' ') ||
        (typeof v.lector === 'object' ? [texto(v.lector,'nombre'),texto(v.lector,'apellido')].filter(Boolean).join(' ') : '') || 'Lector sin datos';
    const tituloLibro = v => texto(v, 'tituloLibro', 'titulo') ||
        (typeof v.libro === 'object' ? texto(v.libro,'titulo') : '') || 'Material sin título';
    const extras = (v, claves) => claves.map(([etiqueta, clave]) => [etiqueta, primero(v,...clave)])
        .filter(([,valor]) => valor !== null && valor !== undefined && valor !== '');

    // Devueltos: origen historial_transacciones. Abiertos: origen prestamo.
    // El préstamo cerrado NO se suma de nuevo: la devolución ya lo representa.
    const convertirHistorial = h => ({
        id: texto(h,'idHistorial','id'), tipo: 'DEVOLUCION',
        fecha: primero(h,'fechaDevolucionReal','fechaDevolucion','fechaEntrega'),
        lector: detallesLector(h), libro: tituloLibro(h),
        legajo: texto(h,'legajo','legajoLector'), inventario: texto(h,'numeroInventario','inventario'),
        datos: h,
        campos: extras(h, [
            ['ID de transacción',['idHistorial','id']],
            ['ID del ejemplar',['idEjemplar']], ['ID del lector',['idLector']],
            ['ID de bibliotecaria',['idBibliotecaria']],
            ['Bibliotecaria',['nombreBibliotecaria']],
            ['Legajo',['legajo','legajoLector']], ['Inventario',['numeroInventario','inventario']],
            ['Fecha de préstamo',['fechaPrestamo']],
            ['Fecha de devolución',['fechaDevolucionReal','fechaDevolucion']],
            ['Estado de entrega',['estadoEntrega']],['Observaciones',['observaciones']]
        ])
    });
    const convertirPrestamo = p => {
        const vencido = p.estadoPrestamo === 'VENCIDO' ||
            (p.estadoPrestamo === 'ACTIVO' && Number(p.diasAtraso) > 0);
        return {
            id: texto(p,'idPrestamo','id'), tipo: vencido ? 'VENCIDO' : 'PRESTAMO',
            fecha: primero(p,'fechaPrestamo'), lector: detallesLector(p), libro: tituloLibro(p),
            legajo: texto(p,'legajo','legajoLector'), inventario: texto(p,'numeroInventario'),
            campos: extras(p, [
                ['ID de préstamo',['idPrestamo','id']],['Legajo',['legajo']],
                ['Inventario',['numeroInventario']],['Tipo',['tipoPrestamo']],
                ['Fecha de préstamo',['fechaPrestamo']],['Fecha límite',['fechaLimiteEntrega']],
                ['Días de atraso',['diasAtraso']],['Estado',['estadoPrestamo']]
            ])
        };
    };

    async function traerTodo(url, parametros = {}) {
        // La API limita cada página a 100; se recorren todas para no omitir registros.
        const primero = await Api.get(url, {...parametros, pagina:0, tamanio:TAMANIO_API});
        if (!Array.isArray(primero.contenido)) throw new Error(`Respuesta inesperada de ${url}`);
        const total = Number(primero.totalPaginas) || 1;
        const resultados = [...primero.contenido];
        for (let pag = 1; pag < total; pag += 1) {
            const r = await Api.get(url, {...parametros, pagina:pag, tamanio:TAMANIO_API});
            if (!Array.isArray(r.contenido)) throw new Error(`Respuesta inesperada de ${url}`);
            resultados.push(...r.contenido);
        }
        return resultados;
    }

    function pintar() {
        const consulta = buscador.value.trim().toLocaleLowerCase('es');
        const filtrados = eventos.filter(e =>
            (tipo === 'TODOS' || e.tipo === tipo) &&
            (!consulta || [e.lector,e.libro,e.legajo,e.inventario,e.id].some(v => String(v).toLocaleLowerCase('es').includes(consulta)))
        );
        const totalPaginas = Math.ceil(filtrados.length/TAMANIO_VISTA);
        paginaVista = Math.min(paginaVista, Math.max(0,totalPaginas-1));
        const pagina = filtrados.slice(paginaVista*TAMANIO_VISTA,(paginaVista+1)*TAMANIO_VISTA);
        conteo.textContent = `${filtrados.length} ${filtrados.length === 1 ? 'evento' : 'eventos'}`;
        const clase = {DEVOLUCION:'green',PRESTAMO:'blue',VENCIDO:'red'};
        const insignia = {DEVOLUCION:'success',PRESTAMO:'primary',VENCIDO:'danger'};
        const icono = {DEVOLUCION:'ph-check-circle',PRESTAMO:'ph-book',VENCIDO:'ph-warning-circle'};
        const titulo = {DEVOLUCION:'Devolución',PRESTAMO:'Préstamo',VENCIDO:'Vencido'};
        lista.innerHTML = pagina.length ? pagina.map(e => `
            <div class="timeline-item">
                <div class="timeline-icon-wrapper ${clase[e.tipo]}"><i class="ph ${icono[e.tipo]}"></i></div>
                <div class="timeline-content">
                    <div class="timeline-row-top">
                        <span class="badge-status ${insignia[e.tipo]}">${titulo[e.tipo]}</span>
                        <strong class="user-name-item">${mostrar(e.lector)}</strong>
                        <span class="date-item">${mostrar(fecha(e.fecha))}</span>
                    </div>
                    <div class="timeline-row-bottom"><i class="ph ph-book-open book-icon"></i><span>${mostrar(e.libro)}</span></div>
                    ${e.tipo === 'DEVOLUCION' && texto(e.datos,'estadoEntrega') ? `<div class="historial-detalles"><span>Estado: ${mostrar(texto(e.datos,'estadoEntrega').replaceAll('_',' '))}</span>${texto(e.datos,'observaciones') ? `<span>Observaciones: ${mostrar(texto(e.datos,'observaciones'))}</span>` : ''}</div>` : ''}
                    <details class="historial-extras"><summary>Ver detalles del movimiento</summary>
                        <dl>${e.campos.map(([k,v]) => `<dt>${mostrar(k)}</dt><dd>${mostrar(v)}</dd>`).join('')}</dl>
                    </details>
                </div>
            </div>`).join('') : '<p class="historial-mensaje">No hay movimientos para mostrar.</p>';
        paginacion.innerHTML = totalPaginas > 1 ? `
            <span>Página ${paginaVista+1} de ${totalPaginas}</span>
            <div><button type="button" data-pagina="${paginaVista-1}" ${paginaVista===0?'disabled':''}>Anterior</button>
            <button type="button" data-pagina="${paginaVista+1}" ${paginaVista>=totalPaginas-1?'disabled':''}>Siguiente</button></div>` : '';
    }

    async function cargar() {
        lista.innerHTML = '<p class="historial-mensaje">Consultando movimientos de la biblioteca...</p>';
        paginacion.innerHTML = '';
        document.getElementById('actualizarHistorial').disabled = true;
        try {
            const [historial, prestamos] = await Promise.all([
                traerTodo('/api/historial'), traerTodo('/api/prestamos')
            ]);
            const abiertos = prestamos.filter(p => p.estadoPrestamo !== 'DEVUELTO');
            eventos = [...historial.map(convertirHistorial),...abiertos.map(convertirPrestamo)]
                .sort((a,b) => String(b.fecha || '').localeCompare(String(a.fecha || '')));
            const numDevoluciones = historial.length;
            const numVencidos = eventos.filter(e => e.tipo === 'VENCIDO').length;
            document.getElementById('kpiTotal').textContent = eventos.length;
            document.getElementById('kpiPrestamos').textContent = abiertos.length;
            document.getElementById('kpiDevoluciones').textContent = numDevoluciones;
            document.getElementById('kpiVencidos').textContent = numVencidos;
            paginaVista = 0;
            pintar();
        } catch (e) {
            lista.innerHTML = '<p class="historial-mensaje" role="alert">No se pudo cargar el historial. Revisá la conexión con el servidor.</p>';
            conteo.textContent = 'Sin datos';
            Ui.toast(e.message,'error');
        } finally {
            document.getElementById('actualizarHistorial').disabled = false;
        }
    }

    buscador.addEventListener('input', Ui.debounce(() => {paginaVista=0;pintar();},220));
    document.getElementById('filtrosHistorial').addEventListener('click', e => {
        const boton = e.target.closest('button[data-tipo]');
        if (!boton) return;
        tipo = boton.dataset.tipo;
        document.querySelectorAll('.historial-filtro').forEach(b => b.classList.toggle('seleccionado',b===boton));
        paginaVista=0;pintar();
    });
    paginacion.addEventListener('click', e => {
        const boton = e.target.closest('button[data-pagina]');
        if (!boton || boton.disabled) return;
        paginaVista=Number(boton.dataset.pagina);
        pintar();
    });
    document.getElementById('actualizarHistorial').addEventListener('click',cargar);
    await cargar();
});
