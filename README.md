# Librería Cielo - sistema de venta

Adaptación destinada a Librería Cielo. Uso restringido conforme a [USO_RESTRINGIDO.md](USO_RESTRINGIDO.md); se respetan derechos y licencias de terceros. Publicar el código no impide que otras personas lo consulten o hagan fork en GitHub.

Este repositorio público contiene código y ejemplos de configuración. No contiene credenciales reales, respaldos, historial anterior ni los paquetes privados del equipo. MFA de aplicación, HTTPS e integración de las tres cuentas requieren completar su implementación y verificación; no se anuncian como controles ya finalizados.

Distribución: A (frontend, gateway, configuración y registro), B (Auth y Cliente), C (Book y Venta). Para desplegar se necesita configurar redes, secretos y dependencias. Los colaboradores reciben instrucciones específicas y sus secretos por separado.

## Documentación técnica del sistema base adaptado

# Sistema de Venta de Librería

Aplicación académica con cuatro microservicios de negocio, Spring Boot, MySQL, Angular, Eureka, Config Server y API Gateway.

## Arranque local

Requisitos: Java 17, Maven 3.9, Docker con Linux containers, Docker Compose v2 y PowerShell 7. El frontend se compila dentro de Docker con Node 22; no requiere Node instalado en el host.

```powershell
.\tools\start-local.ps1
.\tools\smoke-local.ps1
```

Abrir **http://localhost:4200**. Las credenciales de `admin`, `cajero` y `almacen` se generan aleatoriamente y se guardan en **`.local/ACCESO_LOCAL.md`**, fuera de Git. No hay una contraseña de producción predefinida. Si se reutiliza el volumen MySQL, los usuarios existentes mantienen su contraseña.

`start-all.bat` ejecuta el mismo arranque. Para iniciar de nuevo con los JAR ya compilados: `tools/start-local.ps1 -SkipBuild`.

```powershell
docker compose ps
docker compose logs --tail 80 auth book cliente venta gateway
docker compose stop
# Para volver a iniciar, conservando todos los datos:
docker compose up -d
```

No usar `docker compose down -v` salvo que se quiera eliminar explícitamente la base de datos y las imágenes de esta instalación.

## Componentes

| Componente | Puerto interno | Función |
|---|---:|---|
| Frontend / Nginx | 80 | Angular; proxy `/api` hacia Gateway |
| Gateway | 8095 | Rutas explícitas y validación JWT |
| Registry | 8090 | Descubrimiento Eureka |
| Config Server | 7071 | Configuración nativa autenticada de `config-data` |
| Auth | 8081 | Usuarios y autenticación |
| Book | 8082 | Productos, imágenes y reservas de stock |
| Cliente | 8083 | Clientes y vendedores |
| Venta | 8084 | Carritos, ventas, servicios y PDF |
| MySQL | 3306 | Cuatro bases y usuarios con permisos separados |

El arranque local solo publica localhost:4200. Los puertos internos y MySQL no se publican en el host. Los datos MySQL y las imágenes utilizan volúmenes persistentes independientes. Los servicios consumen Config Server y se registran en Eureka usando direcciones alcanzables dentro de la red Docker.

## Seguridad y configuración

- Crear `.env` con `tools/init-local.ps1`; `.env.example` contiene solamente nombres y marcadores. Nunca compartir ni versionar `.env` o `.local`.
- JWT HS256 con clave aleatoria de al menos 32 bytes y duración de una hora. Cada servicio valida el token y aplica permisos de administrador, cajero o almacén.
- Crear/listar/eliminar usuarios y vincular sus perfiles requiere administrador. El registro público está deshabilitado.
- El gateway no genera rutas automáticas para todos los servicios y no publica `/internal/stock`.
- Los usuarios iniciales se crean solo al habilitar `BOOTSTRAP_ENABLED`; no se cambian contraseñas existentes al reiniciar.
- Los datos de demostración se activan con `SEED_DEMO_DATA`. El Compose local los habilita; desactivarlos antes del primer arranque de una instalación limpia.
- La interfaz utiliza `/api`, sin dependencias de Railway ni direcciones localhost incrustadas para las API de producción.
- El entorno local usa HTTP en loopback. Para acceso público en AWS es obligatorio preparar HTTPS y restringir las redes antes de exponer la aplicación.

## Ventas y recuperación

`POST /venta/realizar` requiere un UUID en `Idempotency-Key`, `clienteNombre` y `metodoPago`. Los métodos son `EFECTIVO`, `YAPE_PLIN` y `TRANSFERENCIA`; esta última requiere `nroOperacion`. Un `clienteId` registrado es opcional.

La venta se guarda primero como `PENDIENTE` con precios, productos e identificador de operación. Book reserva todo el stock en una transacción con bloqueo de productos y registra ese mismo identificador. Repetir la reserva no vuelve a descontar existencias. Venta confirma después su estado como `COMPLETADA` y elimina únicamente los ítems consumidos del carrito.

Una respuesta de red ambigua deja la venta pendiente, sin presentarla como completada o cancelarla a ciegas. La pantalla Ventas permite reintentar; también existe `POST /venta/{id}/reintentar`, limitado al propietario. Si Book rechaza definitivamente el stock o el precio, la venta pasa a `CANCELADA` y conserva el carrito para revisarlo. Las operaciones pendientes necesitan recuperación; no existe un trabajador automático de conciliación.

Los pagos se registran manualmente: el sistema no integra una pasarela de Yape, Plin o banco. Los PDF son comprobantes académicos y no documentos tributarios autorizados.

## Pruebas

```powershell
mvn -B package
.\tools\smoke-local.ps1
```

Las pruebas Java usan H2 aislado. Incluyen controles de autorización, reserva atómica de varios productos, competencia por la última unidad, reintentos idempotentes y recuperación de una venta con respuesta ambigua. La prueba HTTP usa los servicios reales del Compose y crea una venta local identificable; guarda su informe en `.local/verificacion-api.json`.

Las pruebas antiguas `*IntegrationTest` que requieren endpoints externos están excluidas del ciclo normal de Venta. No equivalen a la verificación HTTP nueva. La validación de dependencias y el hardening de producción son tareas separadas; pasar estas pruebas no certifica ausencia de vulnerabilidades.

