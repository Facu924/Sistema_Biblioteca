/* Conexión de las pantallas con la API de Biblioteca CENT 35.
   Se sirve desde el mismo servidor que la API, por eso la sesión (cookie) funciona sola. */
(function () {
    'use strict';

    // Raíz del sitio, calculada a partir de la ubicación de este mismo archivo (…/comun/api.js)
    const RAIZ = document.currentScript.src.replace(/comun\/api\.js.*$/, '');

    class ApiError extends Error {
        constructor(status, mensaje, campos) {
            super(mensaje);
            this.status = status;
            this.campos = campos || null;
        }
    }

    function mensajePorEstado(status) {
        switch (status) {
            case 400: return 'Los datos enviados no son válidos.';
            case 401: return 'Tu sesión venció. Volvé a iniciar sesión.';
            case 403: return 'No tenés permiso para realizar esta acción.';
            case 404: return 'No se encontró lo que buscabas.';
            case 413: return 'El archivo es demasiado grande.';
            case 423: return 'La cuenta está bloqueada.';
            default: return 'Ocurrió un error en el servidor. Intentá de nuevo.';
        }
    }

    async function pedir(metodo, url, opciones) {
        const { cuerpo, formulario, parametros, sinRedireccion } = opciones || {};
        const config = { method: metodo, headers: {}, credentials: 'same-origin' };

        if (cuerpo !== undefined) {
            config.headers['Content-Type'] = 'application/json';
            config.body = JSON.stringify(cuerpo);
        }
        if (formulario) {
            config.headers['Content-Type'] = 'application/x-www-form-urlencoded';
            config.body = new URLSearchParams(formulario).toString();
        }

        let consulta = '';
        if (parametros) {
            const p = new URLSearchParams();
            Object.entries(parametros).forEach(([clave, valor]) => {
                if (valor !== undefined && valor !== null && valor !== '') p.append(clave, valor);
            });
            const texto = p.toString();
            if (texto) consulta = '?' + texto;
        }

        let respuesta;
        try {
            respuesta = await fetch(url + consulta, config);
        } catch (e) {
            throw new ApiError(0, 'No se pudo conectar con el servidor. Verificá que esté encendido.');
        }

        let datos = null;
        if ((respuesta.headers.get('Content-Type') || '').includes('application/json')) {
            try { datos = await respuesta.json(); } catch (e) { /* respuesta sin cuerpo */ }
        }

        if (!respuesta.ok) {
            if (respuesta.status === 401 && !sinRedireccion) {
                window.location.href = RAIZ + 'Login/login.html';
            }
            throw new ApiError(respuesta.status,
                (datos && datos.error) || mensajePorEstado(respuesta.status),
                datos && datos.campos);
        }
        return datos;
    }

    const Api = {
        ApiError,
        get: (url, parametros, opciones) => pedir('GET', url, { parametros, ...(opciones || {}) }),
        post: (url, cuerpo, opciones) => pedir('POST', url, { cuerpo, ...(opciones || {}) }),
        put: (url, cuerpo, opciones) => pedir('PUT', url, { cuerpo, ...(opciones || {}) }),
        eliminar: (url, opciones) => pedir('DELETE', url, opciones),
        login: (mail, clave) => pedir('POST', '/api/login', { formulario: { mail, clave }, sinRedireccion: true })
    };

    // ---------------------------------------------------------------- sesión
    const ROLES = {
        ROLE_ADMIN: 'Administrador',
        ROLE_BIBLIOTECARIA: 'Bibliotecaria',
        ROLE_LECTOR: 'Lector'
    };

    function destinoPorRol(rol) {
        return rol === 'ROLE_LECTOR'
            ? RAIZ + 'Portal-Usuario/Inicio/portal-usuario.html'
            : RAIZ + 'Portal-Gestion/Dashboard/dashboard.html';
    }

    function pintarUsuario(u) {
        const nombre = (u.nombre + ' ' + u.apellido).trim();
        document.querySelectorAll('.user-name').forEach(el => { el.textContent = nombre; });
        document.querySelectorAll('.user-role').forEach(el => { el.textContent = ROLES[u.tipoUsuario] || ''; });
        const iniciales = ((u.nombre || '?')[0] + (u.apellido || '')[0]).toUpperCase();
        document.querySelectorAll('.user-profile .avatar').forEach(el => { el.textContent = iniciales; });
    }

    /** Oculta lo que el rol actual no puede usar: data-solo="ROLE_ADMIN" o "ROLE_BIBLIOTECARIA,ROLE_ADMIN". */
    function aplicarRoles(rol) {
        document.body.dataset.rol = rol;
        document.querySelectorAll('[data-solo]').forEach(el => {
            const permitidos = el.dataset.solo.split(',').map(s => s.trim());
            if (!permitidos.includes(rol)) el.style.display = 'none';
        });
    }

    const Sesion = {
        usuario: null,

        async cargar(opciones) {
            this.usuario = await pedir('GET', '/api/perfil', opciones);
            return this.usuario;
        },

        /** Verifica la sesión y el rol. Devuelve el usuario, o null si ya se está redirigiendo. */
        async requerir(rolesPermitidos) {
            let u;
            try {
                u = await this.cargar();
            } catch (e) {
                if (e.status === 401) return null; // pedir() ya redirigió al login
                Ui.toast(e.message, 'error');
                return null;
            }
            if (rolesPermitidos && !rolesPermitidos.includes(u.tipoUsuario)) {
                window.location.href = destinoPorRol(u.tipoUsuario);
                return null;
            }
            pintarUsuario(u);
            aplicarRoles(u.tipoUsuario);
            document.querySelectorAll('.logout-btn').forEach(el => {
                el.addEventListener('click', ev => { ev.preventDefault(); Sesion.cerrar(); });
            });
            return u;
        },

        async cerrar() {
            try { await pedir('POST', '/api/logout', { sinRedireccion: true }); } catch (e) { /* igual se sale */ }
            window.location.href = RAIZ + 'Login/login.html';
        },

        destinoPorRol
    };

    // -------------------------------------------------------------------- UI
    function esc(texto) {
        return String(texto === null || texto === undefined ? '' : texto)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function contenedorToasts() {
        let c = document.getElementById('uiToasts');
        if (!c) {
            c = document.createElement('div');
            c.id = 'uiToasts';
            c.className = 'ui-toasts';
            document.body.appendChild(c);
        }
        return c;
    }

    const Ui = {
        esc,

        /** tipo: ok | error | aviso | info */
        toast(mensaje, tipo, milisegundos) {
            const t = document.createElement('div');
            t.className = 'ui-toast ' + (tipo || 'ok');
            t.setAttribute('role', 'status');
            t.textContent = mensaje;
            contenedorToasts().appendChild(t);
            setTimeout(() => t.remove(), milisegundos || 5000);
        },

        abrir(id) {
            const ov = document.getElementById(id);
            if (!ov) return;
            ov.classList.add('abierto');
            ov.setAttribute('aria-hidden', 'false');
        },

        cerrar(id) {
            const ov = document.getElementById(id);
            if (!ov) return;
            ov.classList.remove('abierto');
            ov.setAttribute('aria-hidden', 'true');
        },

        /** Cierra los modales con la X, con "Cancelar", haciendo clic afuera o con Escape. */
        activarModales() {
            document.querySelectorAll('.ui-overlay').forEach(ov => {
                ov.addEventListener('click', ev => {
                    if (ev.target === ov || ev.target.closest('[data-cerrar]')) Ui.cerrar(ov.id);
                });
            });
            document.addEventListener('keydown', ev => {
                if (ev.key === 'Escape') {
                    document.querySelectorAll('.ui-overlay.abierto').forEach(ov => Ui.cerrar(ov.id));
                }
            });
        },

        /** 2026-05-20T10:15:00 -> 2026-05-20 */
        fecha(valor) {
            return valor ? String(valor).substring(0, 10) : '—';
        },

        debounce(funcion, espera) {
            let temporizador;
            return (...args) => {
                clearTimeout(temporizador);
                temporizador = setTimeout(() => funcion(...args), espera || 300);
            };
        }
    };

    window.Api = Api;
    window.Sesion = Sesion;
    window.Ui = Ui;
})();
