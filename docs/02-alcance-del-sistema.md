El proyecto consiste en el desarrollo de un **Sistema Integral de Gestión Bibliotecaria y Repositorio Digital** para la institución, implementado como una aplicación web robusta orientada a optimizar tanto la atención en el mostrador como la autogestión de los usuarios.

El alcance se divide y delimita en los siguientes módulos y capacidades funcionales definitivas:

### 1. Módulo de Autenticación y Gestión de Usuarios (Administrador)

- **Gestión de Cuentas y Accesos:** Alta de usuarios con definición de roles estrictos (`ADMIN`, `BIBLIOTECARIO`, `USUARIO_GENERAL`) y perfiles institucionales diferenciados (carreras para alumnos, categoría "DOCENTE" para profesores, y perfil libre para el personal administrativo) (`HU-01`).
- **Mantenimiento y Auditoría:** Capacidad de realizar bajas lógicas de usuarios para preservar la integridad histórica de los préstamos pasados (`HU-02`) y edición global de perfiles.
- **Seguridad y Control de Acceso (NUEVO):** Monitoreo automático de intentos fallidos de inicio de sesión por parte del sistema, generando un bloqueo temporal de cuenta ante ataques de fuerza bruta (`HU-11`). Herramienta de desbloqueo, blanqueo de credenciales y restablecimiento de estado a cargo exclusivo del Administrador (`HU-03`).

### 2. Módulo de Gestión Operativa de Préstamos (Bibliotecaria/o)

- **Catálogo y Migración:** Administración integral (CRUD) de libros, autores y editoriales, incluyendo un proceso de importación masiva compatible con el estándar bibliotecario *Aguapey* (`RF-BIB-01`).
- **Atención Rápida en Mostrador:** Búsqueda optimizada insensible a mayúsculas, minúsculas y acentos, con soporte para lectura de códigos de barras mediante el número de inventario para registrar préstamos presenciales en segundos (`HU-04`).
- **Validaciones Operativas y Reglas de Negocio (Actualizado):**
    - Verificación obligatoria en mostrador de que el usuario no posea penalizaciones vigentes ni cuentas bloqueadas (`HU-05`).
    - Cálculo automático de fechas de devolución según la modalidad (Aula, Sala o Domicilio).
    - Restricción de stock mínimo que bloquea de forma automática el préstamo domiciliario si solo queda un ejemplar, reservándolo para uso interno en sala (`HU-12`).
    - Control estricto de límite que impide que un usuario supere los 2 materiales físicos en préstamo simultáneo (`HU-13`).
- **Control de Devoluciones y Estados:** Procesamiento de devoluciones, actualización de stock en tiempo real y registro de estados especiales sobre los ejemplares (ej. Extraviado, Dañado) para gestionar su reposición (`HU-06`).

### 3. Módulo de Autogestión y Consulta (Alumnos y Docentes)

- **Búsqueda y Acceso a Contenido Digital:** Consulta pública del catálogo físico y digital, con capacidad de visualizar archivos PDF/EPUB directamente en el navegador (optimizado para su uso en las 10 tablets institucionales) o descargarlos localmente (`HU-07`, `HU-08`).
- **Reservas y Préstamos Condicionados (NUEVO):** Facultad para solicitar reservas bajo colas de espera, sujeto estrictamente a que el usuario mantenga su cuenta activa, sin bloqueos ni penalizaciones pendientes (`HU-09`).
- **Seguimiento Personal:** Visualización exclusiva del historial de préstamos propios (activos, devueltos y vencidos) para un control riguroso de los plazos (`HU-10`).

### 4. Requerimientos Tecnológicos y No Funcionales del Sistema

- **Arquitectura y Backend:** Desarrollado en Java con **Spring Boot**, estructurado en capas (Controladores, Servicios, Repositorios) y aplicando **Spring Data JPA / Hibernate** para aislar la lógica de negocio de las consultas nativas (`RNF-TEC-01`, `RNF-TEC-02`).
- **Seguridad Web:** Implementación de **Spring Security** con control estricto de accesos por roles (**RBAC**) por cada petición HTTP y cifrado de contraseñas mediante **BCrypt** (`RNF-SEC-01`, `RNF-SEC-02`). Credenciales iniciales configuradas mediante variables de entorno (`RNF-SEC-03`).
- **Base de Datos e Integridad:** Motor **MySQL** utilizando restricciones de Clave Primaria, Foránea y Unicidad, con soporte para transacciones **ACID** (rollback automático ante fallos críticos en préstamos/devoluciones) y *collation* `utf8mb4_unicode_ci` para búsquedas sin sensibilidad a acentos (`RNF-REL-01`, `RNF-REL-02`). Los archivos pesados (PDFs) se almacenan en el sistema de archivos del servidor, guardando únicamente la ruta referencial en la base de datos (`RNF-TEC-02`).
- **Rendimiento y Periféricos:** Tiempos de respuesta inferiores a los 2 segundos en red local, alta concurrencia para consultas masivas de alumnos sin bloquear las operaciones rápidas del mostrador, e integración de hardware con lectores de códigos de barras y diseño adaptado a tablets (`RNF-PERF-01`, `RNF-PERF-02`, `RNF-HW-01`).

---

### 5. Límites y Exclusiones de Alcance (Fuera de Alcance)

Para garantizar la viabilidad y entrega en tiempo y forma del proyecto, quedan explícitamente excluidas las siguientes funcionalidades y características:

- **Notificaciones Automatizadas Externas:** El sistema no enviará correos electrónicos automáticos (como alertas de vencimiento por SMTP) ni mensajes de texto o WhatsApp a los usuarios; toda comunicación por moras o reposiciones se gestionará de manera directa a través de los datos de contacto visibles para la bibliotecaria (`RF-BIB-07`).
- **Pasarelas de Pago o Multas Monetarias:** No se contemplará un módulo de cobro de multas en dinero ni integración con pasarelas de pago externas; la sanción del sistema se limita estrictamente al bloqueo de cuenta y penalizaciones por préstamo.
- **Autenticación Externas / OAuth2:** El sistema no integrará servicios de autenticación de terceros (como Google Sign-In, LDAP institucional u Office 365), utilizando un esquema de autenticación propio gestionado mediante base de datos relacional y cifrado BCrypt.
- **Gestión de Inventario Financiero o Compras:** El módulo de proveedores y compras de material bibliográfico queda excluido; el sistema solo administra el catálogo existente, altas de nuevos ejemplares y bajas por expurgo (`RF-BIB-01`, `RF-BIB-06`).
- **Aplicación Móvil Nativa:** El desarrollo se limita estrictamente a una aplicación web responsiva adaptada para navegadores de escritorio y para las 10 tablets institucionales, sin contemplar el desarrollo de apps nativas para Android o iOS (`RNF-HW-01`).