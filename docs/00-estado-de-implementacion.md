# Estado de implementación

*Última actualización: octubre de 2026.*

Este documento relaciona cada requisito con lo que está hecho. **Implementado** significa que existe en la API (backend) y se verificó a mano con las colecciones de Postman. Las pantallas todavía no existen, por lo que lo que depende de ellas figura como parcial o pendiente.

## Requisitos funcionales

| Requisito | Estado | Observaciones |
|---|---|---|
| RF-GEN-01 Gestión de perfil | Implementado | El usuario cambia teléfono, correo y contraseña. Nombre, apellido y legajo los edita solo el Administrador. |
| RF-GEN-02 Bloqueo de cuenta | Implementado | Bloqueo a los 5 intentos fallidos; se mantiene hasta que el Administrador desbloquea. |
| RF-GEN-03 Auditoría de eventos | Implementado | Ver las acciones registradas en `06-entidades-y-relaciones.md`. |
| RF-ADM-01 Alta de usuarios | Implementado | Crea persona, usuario y lector en una sola transacción. |
| RF-ADM-02 Baja de usuarios | Implementado | Baja lógica y reactivación. |
| RF-ADM-03 Visualización global | Implementado | Listado con filtros y paginación. |
| RF-ADM-04 Modificación externa | Implementado | Edita datos, rol, perfil y contraseña. |
| RF-ADM-05 Desbloqueo de cuentas | Implementado | Con contraseña temporal opcional. |
| RF-ADM-06 Auditoría total | Implementado | Solo lectura sobre catálogo, préstamos, mora, penalizaciones, reservas e historial. |
| RF-BIB-01 Catálogo y migración | **Parcial** | El ABM de libros, autores, editoriales y materias está hecho. **Falta la importación masiva desde Aguapey.** |
| RF-BIB-02 Búsqueda optimizada | Implementado en la API | Búsqueda sin acentos ni mayúsculas y consulta por número de inventario. El lector de códigos de barras se resuelve en la interfaz. |
| RF-BIB-03 Registro de préstamos | Implementado | Plazos, usuario habilitado y mora. |
| RF-BIB-04 Stock mínimo | Implementado | |
| RF-BIB-05 Límite de 2 materiales | Implementado | |
| RF-BIB-06 Devoluciones y bajas | Implementado | Estados de entrega, bajas lógicas por expurgo. |
| RF-BIB-07 Auditoría y contacto | Implementado | Historial y listado de mora con teléfono y correo. |
| RF-BIB-08 Gestión de penalizaciones | Implementado | |
| RF-BIB-09 Gestión de reservas | Implementado | |
| RF-BIB-10 Estados de ejemplares | Implementado | |
| RF-ALU-01 Catálogo y material digital | **Parcial** | Búsqueda y stock en tiempo real listos. El servidor entrega los archivos, pero mostrarlos en el navegador depende de la interfaz, y el EPUB necesita un lector. **La subida de archivos no se verificó de punta a punta.** |
| RF-ALU-02 Reserva y préstamo | Implementado | |
| RF-ALU-03 Historial personal | Implementado | |
| RF-ALU-04 Sanciones y reservas propias | Implementado | |
| RF-SIS-01 Mantenimiento diario | Implementado | |

## Requisitos no funcionales

El detalle de cada uno está al final de [`04-requisitos-no-funcionales.md`](04-requisitos-no-funcionales.md). En resumen: seguridad, integridad, transacciones y arquitectura están cumplidos; los tiempos de respuesta y la concurrencia **todavía no se midieron**, y la integración con el lector de códigos de barras queda pendiente de las pantallas.

## Casos de uso

Las historias `HU-01` a `HU-13` (documento original) y `HU-14` a `HU-24` (incorporadas durante el desarrollo) están implementadas en la API. `HU-07` y `HU-08` dependen de la interfaz para la visualización del material.

## Pendientes

1. **Aplicación de gestión** (bibliotecaria y administrador): mostrador, préstamos, devoluciones, catálogo y usuarios.
2. **Aplicación de autogestión** (alumnos y profesores): búsqueda, visualización de PDF y EPUB, reservas e historial.
3. **Importación masiva desde Aguapey.** Requiere un archivo de ejemplo del formato real.
4. **Pruebas automatizadas.** La dependencia de Playwright ya está en `build.gradle`, pero todavía no hay pruebas escritas. Hasta ahora todo se verificó a mano con las colecciones de Postman.
5. **Medición de rendimiento** (`RNF-PERF-01` y `RNF-PERF-02`).
6. **Cierre de sesiones al dar de baja a un usuario** (ver limitaciones en `07-decisiones-de-diseno.md`).
