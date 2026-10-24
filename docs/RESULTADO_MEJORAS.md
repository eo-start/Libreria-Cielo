# Mejoras y verificación local — 22 de octubre de 2026

El proyecto conserva cuatro servicios de negocio (Auth, Book, Cliente y Venta), Gateway, Eureka, Config Server y la interfaz Angular. Se preparó un arranque reproducible con Docker Compose. Esta etapa no despliega AWS ni modifica el repositorio remoto.

## Correcciones

- Autenticación JWT y permisos en cada servicio; registro de usuarios exclusivo del administrador. Claves generadas fuera del código y sin contraseña predeterminada de Config Server.
- Ventas con identificador de operación: reintentar no duplica la venta ni el descuento de stock. Reserva de todos los productos en una transacción, con bloqueo de filas y control de precio y existencias.
- Recuperación explícita de ventas pendientes cuando una respuesta de red es incierta. Carrito protegido mientras existe una venta pendiente.
- Conservación de usuarios, clientes, ventas, carrito e imágenes entre reinicios. Datos de ejemplo únicamente cuando se activa su opción de configuración.
- Interfaz conectada a datos reales; errores visibles, métodos de pago coherentes, cliente asociado a la venta y comprobante académico. Se retiraron datos simulados usados como respuesta silenciosa ante errores.
- Angular actualizado, compilación de producción y auditoría npm de dependencias de producción sin vulnerabilidades reportadas al verificar. Esto no sustituye una auditoría de todas las dependencias Java, imágenes y herramientas de desarrollo.
- Direcciones de servicios actualizadas mediante DNS con caché corta: los cambios de IP al reiniciar Docker no deben mantener rutas antiguas.
- Jenkins archiva artefactos y exige Quality Gate; ya no presenta mensajes de despliegue simulado. Estos pipelines requieren Jenkins/Sonar configurados y no se ejecutaron en esta etapa.

## Evidencia reproducible

Las pruebas Java generaron 34 informes con 210 pruebas, sin fallos, errores ni omisiones. Incluyen pruebas transaccionales con H2 y comprobaciones de autorización. Los informes están en `ms-*/target/surefire-reports`.

`tools/smoke-local.ps1` completó 18 comprobaciones de las API del Docker local con MySQL: autenticación, permisos, pago, stock, reintento de venta y PDF. Su informe se guarda en `.local/verificacion-api.json`.

`python tools/verify-persistence.py` completó 15 comprobaciones: creó datos ficticios identificables, reinició los servicios, comprobó persistencia y enfrentó dos compradores por las últimas unidades con MySQL real. Solo uno completó la compra y el stock final fue cero. Su informe se guarda en `.local/verificacion-persistencia.json`. Estas pruebas escriben datos de demostración en el entorno local.

La interfaz y las API se acceden desde `http://localhost:4200`. Los usuarios iniciales son `admin`, `cajero` y `almacen`; sus contraseñas están únicamente en `.local/ACCESO_LOCAL.md`. No incluir ese archivo ni `.env` al compartir el proyecto.

También se verificó en el navegador el acceso de administrador, el panel, la carga de productos, agregar al carrito y completar una venta con descarga del comprobante. La evidencia visual está en `.local/verificacion-ui.png`.

El paquete `.local/Sistema-Venta-Libreria-equipo.zip` contiene las fuentes actuales sin historial Git, credenciales locales ni archivos compilados. Se regenera con `python tools/export-equipo.py`; el exportador rechaza archivos que contengan alguno de los secretos de la instalación local.

## Alcance y pendientes antes de AWS

La aplicación es viable para una demostración académica de microservicios. La distribución entre las tres cuentas está preparada en `DESPLIEGUE_EQUIPO.md` y `deploy/distributed.yml`; todavía necesita validar permisos de VPC Peering en cada Learner Lab y probar la red entre las tres cuentas. Los créditos de las cuentas no se combinan.

No hay alta disponibilidad, pasarela de pagos, factura electrónica ni conciliador automático de ventas pendientes. Antes de acceso público se requiere HTTPS, reglas de red privadas, respaldos y revisión de dependencias e imágenes. La configuración actual usa una clave de firma JWT compartida; la mejora posterior es firma asimétrica con clave privada exclusiva de Auth.

El cierre de una sesión no reemplaza los respaldos. Conservar ambos dominios de datos al recuperar ventas y reservas. No utilizar `docker compose down -v` para apagar el sistema: borra sus volúmenes.
