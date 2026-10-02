### Listado de Enums

- **`Tipo_Usuario`**: `ROLE_ADMIN`, `ROLE_BIBLIOTECARIA`, `ROLE_LECTOR`
- **`Tipo_Lector`**: `ALUMNO`, `PROFESOR`, `PERSONAL_ADMINISTRATIVO`
- **`Estado_Prestamo`**: `ACTIVO`, `DEVUELTO`, `VENCIDO`
- **`Tipo_Prestamo`**: `AULA`, `SALA`, `DOMICILIO`
- **`Estado_Reserva`**: `PENDIENTE`, `COMPLETADA`, `CANCELADA`
- **`Estado_Ejemplar`**: `DISPONIBLE`, `PRESTADO`, `DANADO`, `EXTRAVIADO`, `BAJA_LOGICA`
- **`Estado_Entrega`**: `DEVUELTO_OK`, `DEVUELTO_CON_DANO`, `PERDIDO`

### Modelo de Entidades por Módulos

#### 1. Módulo de Identidad, Seguridad y Accesos

- **`persona`** (Datos personales base y legajo unificado para toda la institución)
    - `id_persona` (Long, PK)
    - `nombre` (String)
    - `apellido` (String)
    - `telefono` (String, Nullable)
    - `legajo` (String, Unique)
- **`usuario`** (Credenciales y control de accesos al sistema)
    - `id_usuario` (Long, PK)
    - `id_persona` (Long, FK -> `persona`, Unique)
    - `mail` (String, Unique)
    - `contrasena` (String) — *Hash BCrypt*
    - `tipo_usuario` (Enum: `Tipo_Usuario`)
    - `intentos_fallidos` (Integer, Default: 0)
    - `cuenta_bloqueada` (Boolean, Default: false)
    - `activo` (Boolean, Default: true) — *Baja lógica*
- **`lector`** (Perfil institucional específico para quienes toman préstamos)
    - `id_lector` (Long, PK)
    - `id_persona` (Long, FK -> `persona`, Unique)
    - `tipo_lector` (Enum: `Tipo_Lector`)
    - `perfil_institucional` (String, Nullable) — *Almacena la carrera (Alumno) o la categoría "DOCENTE" (Profesor); queda vacío para el personal administrativo*

#### 2. Módulo de Catálogo e Inventario (Formato Aguapey / MARC)

- **`autor`**
    - `id_autor` (Long, PK)
    - `nombre_autor` (String) — *Formato Apellido, Nombre*
- **`editorial`**
    - `id_editorial` (Long, PK)
    - `nombre_editorial` (String)
- **`materia_cdu`** (Clasificación temática)
    - `id_materia` (Long, PK)
    - `codigo_cdu` (String) — *Ej: 004.7, 821.134*
    - `nombre_tema` (String)
- **`libro`** (Datos bibliográficos puros)
    - `id_libro` (Long, PK)
    - `isbn` (String, Nullable)
    - `titulo` (String)
    - `id_autor` (Long, FK -> `autor`)
    - `id_editorial` (Long, FK -> `editorial`)
    - `id_materia` (Long, FK -> `materia_cdu`)
    - `anio_publicacion` (Integer)
    - `numero_paginas` (Integer)
    - `edicion` (String, Nullable)
    - `resumen` (Text / String Largo, Nullable) — *Sinopsis o descripción del material*
    - `es_digital` (Boolean, Default: false)
    - `ruta_archivo_digital` (String, Nullable) — *Ruta del PDF/EPUB en el servidor*
- **`ejemplar_fisico`** (Control de copias físicas individuales)
    - `id_ejemplar` (Long, PK)
    - `id_libro` (Long, FK -> `libro`)
    - `numero_inventario` (String, Unique) — *Clave para lectura por código de barras*
    - `ubicacion_estante` (String) — *Topografía / Ubicación física*
    - `estado_ejemplar` (Enum: `Estado_Ejemplar`)

#### 3. Módulo de Circulación, Préstamos y Sanciones

- **`prestamo`** (Operación de mostrador; tras la devolución se conserva con estado `DEVUELTO`)
    - `id_prestamo` (Long, PK)
    - `id_ejemplar` (Long, FK -> `ejemplar_fisico`)
    - `id_lector` (Long, FK -> `lector`)
    - `id_bibliotecaria` (Long, FK -> `usuario`) — *Operador que registró la salida*
    - `fecha_prestamo` (LocalDateTime)
    - `fecha_limite_entrega` (LocalDate)
    - `fecha_devolucion_real` (LocalDateTime, Nullable) — *Se completa al devolver*
    - `tipo_prestamo` (Enum: `Tipo_Prestamo`)
    - `estado_prestamo` (Enum: `Estado_Prestamo`)
- **`reserva`** (Colas de espera de materiales)
    - `id_reserva` (Long, PK)
    - `id_lector` (Long, FK -> `lector`)
    - `id_libro` (Long, FK -> `libro`)
    - `fecha_solicitud` (LocalDateTime)
    - `estado_reserva` (Enum: `Estado_Reserva`)
- **`historial_transacciones`** (Registro permanente tras la devolución)
    - `id_historial` (Long, PK)
    - `id_ejemplar` (Long, FK -> `ejemplar_fisico`)
    - `id_lector` (Long, FK -> `lector`)
    - `id_bibliotecaria` (Long, FK -> `usuario`)
    - `fecha_prestamo` (LocalDateTime)
    - `fecha_devolucion_real` (LocalDateTime)
    - `estado_entrega` (Enum: `Estado_Entrega`)
    - `observaciones` (String, Nullable) — *Detalles de daños o incidencias*
- **`penalizacion`** (Control de sanciones y bloqueos)
    - `id_penalizacion` (Long, PK)
    - `id_lector` (Long, FK -> `lector`)
    - `id_historial` (Long, FK -> `historial_transacciones`, Nullable)
    - `motivo` (String)
    - `fecha_inicio` (LocalDate)
    - `fecha_fin` (LocalDate, Nullable)
    - `activa` (Boolean, Default: true)

#### 4. Módulo de Auditoría y Reportes

- **`auditoria_sistema`** (Registro de eventos críticos para el Administrador)
    - `id_auditoria` (Long, PK)
    - `id_usuario` (Long, FK -> `usuario`, Nullable)
    - `accion` (String) — *Ej: "DESBLOQUEO_CUENTA", "BAJA_LOGICA_USUARIO", "EXPORTACION_REPORTE"*
    - `detalles` (String)
    - `fecha_hora` (LocalDateTime)
    - `ip_origen` (String, Nullable)

---

- **`persona` y `usuario` (1 a 1):** Cada persona registrada en la institución cuenta con exactamente una cuenta de acceso al sistema, y cada credencial de usuario pertenece de manera exclusiva a una única persona física.
- **`persona` y `lector` (1 a 0..1):** Una persona puede tener a lo sumo un perfil de lector si cumple el rol de alumno o profesor y retira material. Aquellos usuarios que sean administradores o bibliotecarios puros y no requieran realizar préstamos no poseen un registro en esta tabla.
- **`autor` y `libro` (1 a N):** Un autor puede escribir o estar asociado a múltiples libros dentro del catálogo, pero cada libro en la base de datos se vincula a un único autor principal.
- **`editorial` y `libro` (1 a N):** Una editorial publica o edita múltiples libros, mientras que cada libro individual pertenece a una sola editorial.
- **`materia_cdu` y `libro` (1 a N):** Una clasificación temática (código CDU) agrupa a muchos libros bajo una misma materia, pero cada libro responde a una única temática específica.
- **`libro` y `ejemplar_fisico` (1 a N):** Un libro (entendido como la obra bibliográfica abstracta) cuenta con una o varias copias físicas reales en la estantería. A su vez, cada ejemplar físico responde de forma inequívoca a un único libro base del catálogo.
- **`lector` y `prestamo` (1 a N):** Un lector puede acumular múltiples préstamos a lo largo de su paso por la institución, pero un préstamo activo en un momento dado pertenece a un único lector.
- **`ejemplar_fisico` y `prestamo` (1 a N):** Un ejemplar físico específico pasa por múltiples préstamos a lo largo del tiempo, pero en un instante determinado solo puede estar asociado a un único préstamo activo.
- **`usuario` (Bibliotecaria/o) y `prestamo` (1 a N):** Una bibliotecaria registra múltiples operaciones de salida en el mostrador, y cada préstamo activo guarda el registro de qué operador institucional procesó la transacción.
- **`lector` y `reserva` (1 a N):** Un lector puede realizar varias solicitudes de reserva de materiales en cola de espera, y cada reserva apunta a un único lector.
- **`libro` y `reserva` (1 a N):** Un título de libro puede tener múltiples reservas activas en su cola de espera, mientras que una solicitud de reserva específica apunta a un único libro base.
- **`lector` y `historial_transacciones` (1 a N):** Un lector acumula un registro permanente de todas sus devoluciones y transacciones finalizadas a lo largo del tiempo.
- **`ejemplar_fisico` y `historial_transacciones` (1 a N):** Un ejemplar físico acumula todo su recorrido histórico de idas, vueltas, daños o pérdidas en el sistema.
- **`usuario` (Bibliotecaria/o) y `historial_transacciones` (1 a N):** Una bibliotecaria interviene y queda registrada en múltiples cierres de transacciones históricas.
- **`lector` y `penalizacion` (1 a N):** Un lector puede recibir diversas sanciones o penalizaciones a lo largo de su cursada debido a moras, retrasos o daños.
- **`historial_transacciones` y `penalizacion` (1 a 0..1):** Una penalización puede estar opcionalmente asociada a una transacción específica del historial (por ejemplo, cuando una devolución tardía o con daño origina directamente la sanción).
- **`usuario` y `auditoria_sistema` (1 a 0..N):** Un usuario (generalmente con rol de administrador) puede realizar múltiples acciones en el sistema que generan eventos de auditoría. Un registro de auditoría apunta a un usuario responsable, o bien puede quedar sin usuario asociado si el evento fue disparado de manera automática por el sistema.

---

### Notas de implementación

**Cambios respecto al modelo original**

- Los valores de enum no llevan eñe (`DANADO`, `DEVUELTO_CON_DANO`) para evitar problemas de codificación en Windows.
- `lector` tiene el campo `tipo_lector`, que resuelve la diferencia entre el enum `Tipo_Lector` y `perfil_institucional`.
- `prestamo` tiene el campo `fecha_devolucion_real`. Así, los préstamos devueltos siguen apareciendo en el listado del lector con su fecha de devolución y su atraso, sin consultar otra tabla.

**Préstamo e historial.** Al devolver, el préstamo no se borra: pasa a `DEVUELTO`. Además se crea un registro en `historial_transacciones` con el estado de entrega y las observaciones. `Estado_Prestamo` significa: `ACTIVO` y `VENCIDO` (todavía no volvió, ambos cuentan para el límite de 2) y `DEVUELTO`.

**Restricciones de unicidad:** `persona.legajo`, `usuario.mail`, `usuario.id_persona`, `lector.id_persona` y `ejemplar_fisico.numero_inventario`.

**Penalizaciones.** Una penalización está *vigente* si `activa` es verdadero y `fecha_fin` es nula o no es anterior a hoy. Al levantarla se pone `activa` en falso y se registra `fecha_fin`. La tarea diaria desactiva las que ya cumplieron su plazo. `id_historial` queda nulo en las sanciones manuales.

**Libros digitales.** `es_digital` es verdadero mientras el libro tenga archivo. `ruta_archivo_digital` guarda solo el nombre del archivo; la carpeta se define con `app.storage.path`.

**Datos normalizados.** Los correos se guardan en minúsculas y sin espacios. Las contraseñas se guardan con BCrypt.

**Acciones registradas en `auditoria_sistema`:** `ALTA_USUARIO`, `EDICION_USUARIO`, `BAJA_LOGICA_USUARIO`, `REACTIVACION_USUARIO`, `BLOQUEO_AUTOMATICO`, `DESBLOQUEO_CUENTA`, `CAMBIO_MAIL`, `CAMBIO_CONTRASENA`, `BAJA_LIBRO`, `CAMBIO_ESTADO_EJEMPLAR`, `PENALIZACION_CREADA` y `PENALIZACION_LEVANTADA`. En `BLOQUEO_AUTOMATICO`, `id_usuario` queda nulo porque lo dispara el sistema.
