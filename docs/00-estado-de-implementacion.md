# Estado de implementación

*Última actualización: noviembre de 2026.*

Este documento relaciona cada requisito con lo que está hecho. En la tabla siguiente, **Implementado** se refiere principalmente a la API (backend), verificada manualmente mediante Postman. El avance de las interfaces y de las pruebas automatizadas se informa por separado, para no confundir la existencia del código con la validación de un flujo completo.

**Alcance de esta actualización:** se preparó la integración del historial de préstamos y se incorporó la pantalla «Lectores y Sanciones» al frontend. Estas funciones todavía requieren verificación integral contra el servidor y la base de datos reales. Se desarrollaron y ejecutaron pruebas automatizadas con Playwright fuera de la tarea `test` de Gradle; el alcance y los resultados de esas pruebas no se detallan aquí porque no se adjuntaron sus archivos ni su informe.

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

## Estado de las interfaces de gestión

| Pantalla | Situación a noviembre de 2026 |
|---|---|
| Ingreso, panel principal, catálogo y préstamos/devoluciones | Conectados con la API en las etapas anteriores; conviene mantener las verificaciones de regresión. |
| Historial de préstamos | Se sustituyeron los registros estáticos por consultas a `/api/historial` y `/api/prestamos`, con contadores, filtros y detalle. **Pendiente:** probar la integración de punta a punta con registros reales y confirmar el formato exacto de las respuestas. |
| Lectores y Sanciones | Nueva pantalla con pestañas para buscar lectores por legajo y consultar, crear y levantar penalizaciones mediante la API existente. **Pendiente:** verificar la integración, los mensajes de error y los permisos reales. |
| Usuarios y Mi Perfil | Hay archivos de interfaz en el frontend; su funcionamiento integral no queda validado en esta actualización. |
| Menú y visibilidad por rol | Se incorporaron accesos a la nueva sección en pantallas de gestión. **Pendiente:** unificar el menú en todas las pantallas y comprobar la visibilidad según el rol, además de los permisos de Spring Security. |

## Pruebas automatizadas

- Se desarrollaron y ejecutaron pruebas automatizadas con **Playwright** por separado; el usuario informó que finalizaron correctamente.
- Se ejecutó `gradlew clean test` y terminó correctamente después de retirar el test de contexto predeterminado de Spring Boot que fallaba.
- **Estas dos comprobaciones son independientes:** que Gradle termine correctamente no demuestra que haya ejecutado los tests de Playwright.
- **Pendiente:** incorporar aquí las rutas, casos concretos y resultados de Playwright cuando se confirme su estructura definitiva en el repositorio; verificar los nuevos flujos contra MySQL.

## Pendientes

1. **Verificación integral de las pantallas nuevas:** historial y Lectores y Sanciones con sesión real, datos persistidos, paginación, filtros y errores.
2. **Unificación de menú y permisos por rol:** «Usuarios» para administración; gestión de sanciones para bibliotecaria; restringir todas las operaciones desde el backend.
3. **Aplicación de autogestión** (alumnos y profesores). Existe el diseño de la pantalla de inicio con datos de ejemplo. **Pendiente:** conectarla (búsqueda, PDF y EPUB, reservas) y diseñar «Mis préstamos e historial» y «Mis reservas».
4. **Importación masiva desde Aguapey.** Requiere un archivo de ejemplo del formato real.
5. **Completar la documentación de pruebas automatizadas:** ubicación, ejecución específica y cobertura de los tests de Playwright.
6. **Medición de rendimiento** (`RNF-PERF-01` y `RNF-PERF-02`).
7. **Cierre de sesiones al dar de baja a un usuario** (ver limitaciones en `07-decisiones-de-diseno.md`).
