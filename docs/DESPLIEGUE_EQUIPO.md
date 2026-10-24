# Despliegue del equipo en AWS Academy

Este documento prepara un despliegue futuro. No crea recursos AWS. Primero se verifica la aplicación local con `tools/start-local.ps1` y `tools/smoke-local.ps1`.

## Reparto

| Nodo / cuenta | Componentes | Datos persistentes |
|---|---|---|
| A: coordinador | Frontend, Gateway, Registry, Config Server | Configuración |
| B: compañero 1 | Auth y Cliente | MySQL: ms_auth y ms_cliente |
| C: compañero 2 | Book y Venta | MySQL: ms_book y ms_venta; volumen de imágenes |

Todos utilizan exactamente la misma versión del código. Los microservicios mantienen sus procesos, API y bases separadas. Cada cuenta conserva su presupuesto individual; no se suman créditos entre cuentas.

## Antes de crear recursos

1. Confirmar en los tres Learner Labs que EC2, el tipo de instancia elegido, VPC Peering y su aceptación están permitidos. Una pantalla accesible no demuestra permiso de creación. En la auditoría no fue posible leer completamente las políticas IAM del laboratorio.
2. Elegir una sola región permitida, preferiblemente us-east-1. Como punto de partida para medir: una t3.medium por nodo. El tamaño definitivo depende de memoria y CPU observadas; esta distribución no ofrece alta disponibilidad.
3. Usar VPC con CIDR no superpuestos, por ejemplo A=10.10.0.0/16, B=10.20.0.0/16 y C=10.30.0.0/16. No asumir que tres VPC predeterminadas pueden interconectarse.
4. Establecer peering A–B, A–C y B–C, rutas en ambos extremos y DNS cuando corresponda. Peering no es transitivo. No utilizar la IP pública para Eureka o MySQL.
5. Asociar LabInstanceProfile a las EC2 para administración con SSM cuando el laboratorio lo permita. No crear usuarios IAM, roles ni claves permanentes para este proyecto.
6. Mantener MySQL sin publicar el puerto 3306. Permitir puertos de servicios solo desde los nodos del equipo que los necesitan. No publicar Registry ni Config Server en Internet.

## Puertos privados

| Destino | Orígenes necesarios | Puerto |
|---|---|---|
| A / Registry | A, B, C | 8090 |
| A / Config Server (si se activa importación central) | B, C | 7071 |
| B / Auth | A y C | 8081 |
| B / Cliente | A y B | 8083 |
| C / Book | A y C | 8082 |
| C / Venta | A | 8084 |

Gateway utiliza el registro para encontrar las IP privadas anunciadas en SERVICE_HOST. Los puertos permanecen fijos. Los grupos de seguridad y las rutas deben permitir también las respuestas. La comunicación de datos entre EC2 del equipo debe mantenerse en redes privadas; para un entorno fuera del laboratorio, añadir TLS entre servicios.

## Configuración por nodo

Generar `.env` fuera de Git. Cada nodo necesita `PRIVATE_IP` (su IP privada real) y `NODE_A_IP` (IP privada de A). A, B y C deben usar el mismo `JWT_SECRET` aleatorio de al menos 32 bytes. Compartir ese secreto únicamente mediante un canal privado del equipo; cualquier poseedor podría firmar tokens. Una evolución de seguridad sería firma asimétrica con clave privada exclusiva de Auth.

Los tres nodos también necesitan el mismo `CONFIG_SERVER_PASSWORD` para consultar la configuración de A. Compartir únicamente esos dos valores por un canal privado, sin enviar el archivo `.env` completo: cada cuenta mantiene sus propias contraseñas MySQL. Solo B necesita habilitar la creación inicial de usuarios. Los usuarios creados conservan sus contraseñas al reiniciar; modificar `.env` no cambia una contraseña ya guardada.

Compilar los JAR con Java 17 y `mvn -B package`. El Dockerfile de cada backend usa esos JAR; no ejecuta Maven durante el arranque. El frontend se compila en una imagen Node 22. Se necesita Docker Compose v2 y el archivo `.local/ca.pem`: en Linux puede copiarse `/etc/ssl/certs/ca-certificates.crt`; el script de Windows lo genera desde los certificados confiables del equipo. No desactivar la verificación TLS.

Desde la raíz del proyecto, después de preparar archivos y conectividad:

```bash
# Nodo A
docker compose -f compose.yml -f deploy/distributed.yml up -d --build --no-deps registry config gateway frontend

# Nodo B
docker compose -f compose.yml -f deploy/distributed.yml up -d --build --no-deps mysql auth cliente

# Nodo C
docker compose -f compose.yml -f deploy/distributed.yml up -d --build --no-deps mysql book venta
```

En B y C esperar que MySQL esté saludable; si los servicios arrancan antes de inicializarse la base, su política de reinicio permite volver a intentar. En A esperar el registro de los cuatro servicios antes de probar las API.

El frontend escucha en localhost:4200 del nodo A. Acceder mediante un túnel SSM/SSH hacia ese puerto para una demostración privada. No abrirlo a Internet con contraseñas por HTTP. Para publicación pública, preparar un dominio y HTTPS; eso requiere una etapa de despliegue específica y una nueva estimación de costos.

## Comprobaciones de aceptación

- Login con admin, cajero y almacén. Confirmar 401 sin token y 403 con rol incorrecto, incluso en puertos internos.
- Crear cliente y producto; subir una imagen; consultar el catálogo desde otra cuenta.
- Registrar una venta y comprobar descuento, impuesto, método de pago, stock y comprobante.
- Repetir Idempotency-Key: debe devolver la misma venta y no descontar stock dos veces.
- Interrumpir Book durante una confirmación: la venta debe permanecer PENDIENTE. Reintentar esa venta después de recuperar conectividad.
- Dos compradores intentan adquirir la última unidad: solo uno debe completarla.
- Reiniciar contenedores y comprobar usuarios, clientes, carritos, ventas e imágenes. No borrar volúmenes para reiniciar.

## Cierre y presupuesto

Cada integrante detiene sus procesos y EC2 y revisa el estado de su Learner Lab. No depender únicamente de End Lab para detener RDS u otros recursos. Los discos, respaldos e imágenes continúan ocupando almacenamiento. No ejecutar `docker compose down -v`: elimina los datos de esta instalación.

Mantener una copia de seguridad coherente de MySQL y del volumen book-uploads antes de reemplazar el host. Una venta PENDIENTE y su reserva deben recuperarse conservando ambas bases: no restaurar solo una de ellas a un momento anterior sin reconciliación.

El arranque local incluye datos de demostración mediante SEED_DEMO_DATA=true. Para una instalación limpia desactivar esa variable antes del primer arranque. Las ventas de ejemplo son ficticias, los pagos se registran manualmente y los PDF son comprobantes académicos, no comprobantes electrónicos autorizados.
