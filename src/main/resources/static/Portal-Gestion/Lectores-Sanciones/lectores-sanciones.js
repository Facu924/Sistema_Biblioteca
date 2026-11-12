// Lectores y Sanciones — Biblioteca CENT 35. Sin datos ficticios.
document.addEventListener('DOMContentLoaded', async () => {
  const $ = id => document.getElementById(id);
  const sidebar = $('sidebar');
  $('toggleBtn').addEventListener('click', () => sidebar.classList.toggle('collapsed'));
  const usuario = await Sesion.requerir(['ROLE_BIBLIOTECARIA', 'ROLE_ADMIN']);
  if (!usuario) return;
  const operadora = usuario.tipoUsuario === 'ROLE_BIBLIOTECARIA';
  Ui.activarModales();
  Ui.campana('../Prestamos/prestamo.html');
  const paginaTam = 10;
  let pagina = 0, totalPaginas = 0, filtros = {legajo: '', soloVigentes: ''};
  let lectorActual = null;
  let sanciones = [];
  const mostrarError = (err) => err?.campos ? err.message + ' ' + Object.values(err.campos).join(' ') : err.message;
  const fecha = val => val ? Ui.fecha(val) : 'Sin vencimiento';
  const getId = s => s.idPenalizacion ?? s.id;
  const nombre = s => s.nombreLector || s.nombreCompleto || ([s.nombre, s.apellido].filter(Boolean).join(' ')) || 'Lector';
  const textoSeguro = txt => Ui.esc(txt == null ? '' : txt);

  function pestaña(nombrePestana) {
    const verLectores = nombrePestana === 'lectores';
    $('seccionLectores').hidden = !verLectores;
    $('seccionSanciones').hidden = verLectores;
    $('tabLectores').classList.toggle('active', verLectores);
    $('tabSanciones').classList.toggle('active', !verLectores);
    $('tabLectores').setAttribute('aria-selected', String(verLectores));
    $('tabSanciones').setAttribute('aria-selected', String(!verLectores));
    if (!verLectores) cargarSanciones();
  }
  $('tabLectores').addEventListener('click', () => pestaña('lectores'));
  $('tabSanciones').addEventListener('click', () => pestaña('sanciones'));

  async function consultarLector(legajo) {
    $('fichaLector').hidden = true;
    $('lectorMensaje').hidden = false;
    $('lectorMensaje').textContent = 'Buscando lector…';
    lectorActual = null;
    try {
      const l = await Api.get('/api/lectores', {legajo});
      lectorActual = {...l, legajo: l.legajo || legajo};
      $('lectorNombre').textContent = l.nombre || 'Lector sin nombre';
      $('lectorMeta').textContent = 'Legajo: ' + lectorActual.legajo + ' · ' + (l.tipoLector || 'Sin tipo') + (l.perfilInstitucional ? ' · ' + l.perfilInstitucional : '');
      $('lectorPrestamos').textContent = (l.prestamosAbiertos ?? '—') + ' / ' + (l.limitePrestamos ?? '—');
      $('lectorEstado').textContent = l.habilitado ? 'Habilitado' : 'No habilitado';
      $('lectorEstado').className = 'chip ' + (l.habilitado ? 'ok' : 'bad');
      $('lectorMotivo').textContent = l.habilitado ? 'Habilitado para recibir préstamos' : (l.motivoNoHabilitado || 'Tiene restricciones');
      $('lectorMensaje').hidden = true;
      $('fichaLector').hidden = false;
    } catch(e) { $('lectorMensaje').textContent = mostrarError(e); }
  }
  $('formBuscarLector').addEventListener('submit', e => {
    e.preventDefault();
    const legajo = $('legajoBuscar').value.trim();
    if (legajo) consultarLector(legajo);
  });
  $('btnVerSancionesLector').addEventListener('click', () => {
    $('filtroLegajo').value = lectorActual.legajo;
    $('filtroEstado').value = '';
    filtros = {legajo: lectorActual.legajo, soloVigentes: ''}; pagina = 0;
    pestaña('sanciones');
  });

  function renderSanciones() {
    $('tablaSanciones').innerHTML = sanciones.map(s => {
      const vigente = s.activa === true || s.vigente === true;
      const id = getId(s);
      const legajo = s.legajo || s.legajoLector || '—';
      return `<tr><td><strong>${textoSeguro(nombre(s))}</strong><small>Legajo: ${textoSeguro(legajo)}</small></td>
        <td>${textoSeguro(s.motivo || '—')}</td><td>${Ui.fecha(s.fechaInicio)}</td><td>${fecha(s.fechaFin)}</td>
        <td><span class="status ${vigente ? 'live' : 'done'}">${vigente ? 'Vigente' : 'Finalizada'}</span></td>
        <td>${vigente && operadora && id != null ? `<button type="button" class="secondary" data-levantar="${textoSeguro(id)}"><i class="ph ph-check-circle"></i> Levantar</button>` : '—'}</td></tr>`;
    }).join('');
    $('tablaSanciones').querySelectorAll('[data-levantar]').forEach(btn => btn.addEventListener('click', () => levantar(btn.dataset.levantar)));
  }
  async function cargarSanciones() {
    $('mensajeSanciones').hidden = false;
    $('contenedorTabla').hidden = true;
    $('mensajeSanciones').textContent = 'Cargando sanciones…';
    try {
      const res = await Api.get('/api/penalizaciones', {...filtros, pagina, tamanio: paginaTam});
      sanciones = Array.isArray(res) ? res : (res.contenido || []);
      totalPaginas = Array.isArray(res) ? 1 : (res.totalPaginas ?? 1);
      $('infoPagina').textContent = `Página ${pagina+1} de ${Math.max(totalPaginas,1)} · ${Array.isArray(res) ? sanciones.length : (res.totalElementos ?? sanciones.length)} registros`;
      $('paginaAnterior').disabled = pagina === 0;
      $('paginaSiguiente').disabled = pagina >= totalPaginas-1;
      if (!sanciones.length) { $('mensajeSanciones').textContent = 'No se encontraron sanciones para los filtros seleccionados.'; return; }
      renderSanciones(); $('contenedorTabla').hidden = false; $('mensajeSanciones').hidden = true;
    } catch(e) { $('mensajeSanciones').textContent = mostrarError(e); $('infoPagina').textContent = ''; }
  }
  async function cargarMetricas() {
    try {
      // Si no hay datos suficientes para contar el universo, mostramos guiones en lugar de cifras inventadas.
      const todos = await Api.get('/api/penalizaciones', {pagina: 0, tamanio: 1});
      const vigentes = await Api.get('/api/penalizaciones', {pagina: 0, tamanio: 1, soloVigentes: true});
      const total = todos.totalElementos;
      const cantidadVigente = vigentes.totalElementos;
      $('kpiTotal').textContent = Number.isFinite(total) ? total : '—';
      $('kpiVigentes').textContent = Number.isFinite(cantidadVigente) ? cantidadVigente : '—';
      $('kpiFinalizadas').textContent = Number.isFinite(total) && Number.isFinite(cantidadVigente) ? Math.max(0, total-cantidadVigente) : '—';
    } catch(e) { Ui.toast('No se pudieron actualizar los indicadores.', 'aviso'); }
  }
  $('formFiltros').addEventListener('submit', e => {e.preventDefault(); pagina=0; filtros={legajo:$('filtroLegajo').value.trim(),soloVigentes:$('filtroEstado').value};cargarSanciones();});
  $('btnLimpiarFiltros').addEventListener('click', () => { $('formFiltros').reset(); filtros={legajo:'',soloVigentes:''}; pagina=0; cargarSanciones(); });
  $('paginaAnterior').addEventListener('click', () => { if (pagina>0) {pagina--;cargarSanciones();} });
  $('paginaSiguiente').addEventListener('click', () => { if (pagina<totalPaginas-1) {pagina++;cargarSanciones();} });

  function abrirSancion(legajo) {
    if(!operadora) return;
    $('formSancion').reset();
    $('sancionLegajo').value = legajo || '';
    $('errorSancion').hidden = true;
    Ui.abrir('modalSancion');
    $('sancionMotivo').focus();
  }
  $('btnNuevaSancion').addEventListener('click', () => abrirSancion(''));
  $('btnSancionarLector').addEventListener('click', () => abrirSancion(lectorActual?.legajo));
  $('formSancion').addEventListener('submit', async e => {
    e.preventDefault();
    if (!operadora) return;
    const legajo = $('sancionLegajo').value.trim();
    const motivo = $('sancionMotivo').value.trim();
    const diasTxt = $('sancionDias').value.trim();
    if (!legajo || !motivo || (diasTxt && (!Number.isSafeInteger(Number(diasTxt)) || Number(diasTxt)<1))) {
      $('errorSancion').textContent = 'Ingresá legajo y motivo; la duración debe ser un entero positivo o quedar vacía.';
      $('errorSancion').hidden = false; return;
    }
    $('guardarSancion').disabled = true;
    $('errorSancion').hidden = true;
    try {
      const cuerpo = {legajo, motivo};
      if (diasTxt) cuerpo.dias = Number(diasTxt);
      await Api.post('/api/penalizaciones', cuerpo);
      Ui.cerrar('modalSancion');
      Ui.toast('Sanción registrada correctamente.', 'ok');
      await cargarMetricas();
      if (!$('seccionSanciones').hidden) await cargarSanciones();
      if (lectorActual?.legajo === legajo) await consultarLector(legajo);
    } catch(err) { $('errorSancion').textContent = mostrarError(err); $('errorSancion').hidden = false; }
    finally { $('guardarSancion').disabled = false; }
  });
  async function levantar(id) {
    if (!operadora || !window.confirm('¿Confirmás que querés levantar esta sanción? Esta acción modifica el registro.')) return;
    try {
      await Api.post(`/api/penalizaciones/${encodeURIComponent(id)}/levantar`, {});
      Ui.toast('La sanción fue levantada.', 'ok');
      await Promise.all([cargarMetricas(), cargarSanciones()]);
      if (lectorActual) await consultarLector(lectorActual.legajo);
    } catch(e) { Ui.toast(mostrarError(e), 'error'); }
  }
  await cargarMetricas();
});
