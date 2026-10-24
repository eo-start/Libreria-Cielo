# Guía de despliegue de Librería Amanecer en tres cuentas AWS Academy

## 1. Qué está preparado y qué falta

Esta entrega contiene tres paquetes independientes, los JAR compilados, el frontend construido, Docker Compose, scripts y configuración privada separada. No incluye el historial Git ni utiliza las contraseñas de tu instalación local. Preparar los archivos no equivale a desplegarlos en AWS.

Se comprobó tráfico HTTP privado desde la EC2 del integrante B, 172.31.28.189, hacia el servidor temporal de A, 10.10.1.10:8000. Esto valida esa dirección de red entre A y B; la operación del sistema completo requiere las pruebas de la sección 12. El tercer integrante todavía debe proporcionar sus datos y conectar su VPC.

El dominio exacto está pendiente. El marcador libreria.example.invalid impide activar HTTPS por accidente. No necesitas comprar hosting en Spaceship: el sistema se ejecutará en EC2 y Spaceship administrará el dominio/DNS si utiliza sus servidores autoritativos.

## 2. Reparto definitivo propuesto

| Integrante | Paquete | Procesos | Persistencia | Puertos privados |
|---|---|---|---|---|
| Tú, A | Nodo_A.zip | Frontend/Nginx, Gateway, Eureka, Config Server; Caddy al publicar HTTPS | Configuración y certificados | 8090, 7071 |
| Compañero1, B | Nodo_B.zip | Auth, Cliente, MySQL | ms_auth y ms_cliente | 8081, 8083 |
| Compañero2, C | Nodo_C.zip | Book, Venta, MySQL | ms_book, ms_venta e imágenes | 8082, 8084 |

Auth depende de Cliente; Venta depende de Book y Auth; Gateway consulta los cuatro servicios. Los servicios de B y C consultan Eureka y Config Server en A. Por eso **C necesita comunicarse directamente con B**, además de las conexiones hacia A. No sirve intentar pasar de C a B a través de A: VPC Peering no es transitivo.

La interfaz usa /api en el mismo dominio. Los usuarios no necesitan conocer IP privadas ni abrir puertos de los microservicios desde su navegador.

## 3. Inventario de redes

| Nodo | Cuenta | VPC | CIDR | Estado |
|---|---|---|---|---|
| A | 496739439260 | vpc-00a1c9df7767d10b4 | 10.10.0.0/16 | Creada, Libreria-VPC |
| B | 272742537991 | vpc-0ae95142c5fac4225 | 172.31.0.0/16 | Datos proporcionados y prueba de red recibida |
| C | PENDIENTE | PENDIENTE | 10.20.0.0/16 propuesto | Todavía no verificado |

Peering A–B: pcx-069d7538b01b9ee12. Crear y aceptar A–C y B–C cuando C proporcione su cuenta y VPC. El CIDR de C debe evitar TODOS los bloques de A y B, no solo los principales. Mantener las tres cuentas en us-east-1, Norte de Virginia.

En las tablas efectivas de las subredes de las EC2 configurar:

| Tabla del nodo | Destinos privados | Objetivo |
|---|---|---|
| A | 172.31.0.0/16; 10.20.0.0/16 | Peering A–B; peering A–C, respectivamente |
| B | 10.10.0.0/16; 10.20.0.0/16 | Peering A–B; peering B–C |
| C | 10.10.0.0/16; 172.31.0.0/16 | Peering A–C; peering B–C |

La subred A de prueba es subnet-0f04b019f9e0389c6, 10.10.1.0/24, tabla rtb-08f94bd6cc11a131a. Su ruta A–B ya está creada. Comprueba las asociaciones de subred de cada EC2; editar otra tabla no sirve. La salida 0.0.0.0/0 de una subred pública debe ir al Internet Gateway de SU propia VPC.

## 4. Instancias para el despliegue

Usar como punto inicial **una EC2 t3.medium, x86_64, Ubuntu Server24.04 LTS y20GiB de disco gp3 por nodo**. Confirmar que el Learner Lab permita la AMI y el tipo antes de lanzarlas. Cada cuenta mantiene su propio presupuesto; no se combinan los saldos. El tamaño debe ajustarse según métricas reales.

Las EC2 t3.micro empleadas para probar conectividad no tienen la memoria prevista para este conjunto de contenedores. La instancia de prueba de A, i-0bd436beb50ce5942, usa Amazon Linux2023 y no se convierte automáticamente en el nodo Ubuntu de esta guía. Puedes crear un host Ubuntu nuevo en la misma VPC/subred y actualizar las IP de los .env. No ejecutes el instalador Ubuntu en Amazon Linux ni en Ubuntu26.04.

En cada cuenta:

1. Iniciar el Learner Lab y confirmar la cuenta correcta en la consola. No usar las cuentas temporales de cafetería o Storage Gateway.
2. EC2 → Launch instance; nombre Libreria-Nodo-A, B o C.
3. Elegir Ubuntu Server24.04 LTS oficial, arquitectura x86_64, t3.medium y almacenamiento gp3 de20GiB con cifrado.
4. Seleccionar la VPC y subred acordadas. En esta propuesta académica, las EC2 usan subred pública e IPv4 pública para descargar imágenes y administrar el host; los microservicios se publican únicamente en la IP privada.
5. Seleccionar el par de claves propio del laboratorio, por ejemplo vockey, si se usará SSH. Asociar LabInstanceProfile para SSM cuando esté disponible. No crear usuarios IAM ni compartir claves AWS.
6. Seleccionar el Security Group específico de su nodo según la sección 5.
7. Anotar ID de instancia, IP privada, IP pública, VPC, subred y tabla de rutas.
8. Confirmar las rutas de peering y salida. Compartir solamente los identificadores y las IP con el equipo.

Si se reemplaza la EC2, las IP privadas pueden cambiar. La IP pública normal suele cambiar después de Stop/Start. Antes de publicar el dominio, elegir entre una Elastic IP admitida por el lab o actualizar el registro A después de cada cambio. Una Elastic IP/IPv4 pública tiene costo; no asignarla durante la preparación de archivos.

No se necesita RDS, ALB, NAT Gateway, ECS ni ECR para esta versión de bajo presupuesto. Las bases viven en volúmenes de Docker. No ofrece alta disponibilidad ni recuperación automática de un host perdido.

## 5. Security Groups y acceso

Usar las IP privadas REALES con /32 cuando ya estén asignadas. Mantener salida para consultas DNS, descarga de imágenes, SSM y tráfico de la aplicación. Las ACL personalizadas deben permitir también las respuestas.

| Destino | Entrada TCP | Origen permitido |
|---|---|---|
| A | 8090, 7071 | IP privada de B/32 e IP privada de C/32 |
| B | 8081 | IP privada de A/32 e IP privada de C/32 |
| B | 8083 | IP privada de A/32 y, para diagnóstico, la propia IP de B/32 |
| C | 8082, 8084 | IP privada de A/32 y, para diagnóstico/llamadas propias, IP de C/32 |
| A al publicar | 80,443 | Internet: 0.0.0.0/0 |
| A,B,C si usan SSH | 22 | IP pública actual de su propio administrador/32 |

Para check.sh y llamadas del host a sus contenedores publicados, permitir también la IP privada propia/32 en los puertos privados de su nodo. Las llamadas dentro de la red Docker no necesitan publicar MySQL.

No abrir 3306,8095,7071,8090 ni los puertos de negocio a Internet. MySQL y Gateway no publican puertos del host en estos paquetes. Frontend publica únicamente 127.0.0.1:8080 para comprobación/túnel. Caddy es la única entrada pública HTTP/HTTPS.

La validación anterior usaba TCP8000 desde la VPC de B. Esa regla y el servidor temporal no son parte de la aplicación final; retirarlos cuando terminen las pruebas de red. No reutilizar ese grupo sin revisar reglas.

SSH sirve para administrar; HTTPS protege el acceso web. El tráfico entre servicios de esta versión académica viaja por IP privada con HTTP y JWT; una IP privada no cifra el tráfico. Para uso con información sensible fuera del laboratorio se requiere TLS entre servicios y un endurecimiento adicional.

## 6. Archivos que entrega el coordinador

Cada compañero recibe:

- Su Nodo_B.zip o Nodo_C.zip, sin contraseñas.
- Esta guía y su instrucción personal.
- ÚNICAMENTE su Nodo_B.env o Nodo_C.env, por un canal privado distinto del archivo compartido. Guardarlo como .env en el nodo.

La carpeta PRIVADO_NO_COMPARTIR_COMPLETO es del coordinador. No enviarla entera. Contiene el inventario de secretos, los tres .env y las claves iniciales de admin/cajero/almacen. No son claves de AWS ni de Spaceship y todavía no existen como usuarios en AWS hasta arrancar Auth.

JWT_SECRET y CONFIG_SERVER_PASSWORD deben coincidir en A,B,C. Las claves MySQL son diferentes por dominio y nodo. Cualquier poseedor de JWT_SECRET puede firmar tokens: se confía en los integrantes del equipo. No pegar secretos en chats públicos, capturas, repositorios o informes académicos.

## 7. Instalación y subida por cada integrante

Desde tu computadora, sustituye los valores por los de tu EC2 y usa tu propia clave:

```powershell
scp -i C:\ruta\tu-clave.pem Nodo_B.zip ubuntu@IP_PUBLICA_B:~/
scp -i C:\ruta\tu-clave.pem Nodo_B.env ubuntu@IP_PUBLICA_B:~/
ssh -i C:\ruta\tu-clave.pem ubuntu@IP_PUBLICA_B
```

Usar Nodo_A o Nodo_C según corresponda. En Ubuntu:

```bash
sudo apt-get update
sudo apt-get install -y unzip python3
mkdir -p ~/libreria
unzip -n ~/Nodo_B.zip -d ~/libreria
cd ~/libreria/Nodo_B
mv ~/Nodo_B.env .env
chmod 600 .env
chmod +x *.sh
sudo bash install-docker-ubuntu.sh
```

El paquete contiene artifacts/*.jar ya compilados. El nodo A también incluye frontend-dist; no necesitas Java SDK, Maven, npm ni clonar un repositorio con historial. Docker descargará las imágenes base oficiales fijadas por digest; eso sí necesita salida a Internet.

Si administras exclusivamente por SSM, usa un método de carga privado autorizado por tu cuenta (por ejemplo un bucket S3 privado con lectura desde LabRole). No conviertas un bucket en público para descargar un .env. La EC2 de prueba original de A se lanzó sin par SSH, por lo que no debes intentar usar vockey con ella como si estuviera asociado.

## 8. Completar la configuración

En CADA nodo, editar localmente .env sin mostrarlo en una captura:

- PRIVATE_IP: la IP privada real de esa EC2.
- NODE_A_IP: la IP privada real de A, igual en los tres nodos.
- A: PUBLIC_HOST queda libreria.example.invalid hasta recibir el dominio real.
- B: BOOTSTRAP_ENABLED=true únicamente para crear los tres usuarios iniciales en una base nueva.
- B y C: SEED_DEMO_DATA=false para comenzar con datos propios. Para una demostración con catálogo ficticio, cambiar a true ANTES del primer arranque y documentarlo como datos de prueba.

Los archivos privados generados incluyen como referencia las IP de las pruebas. No garantizan que sean las de las EC2 definitivas. preflight.py comprueba que PRIVATE_IP exista en el host y bloquea un arranque con datos incompletos. No cambiar las claves compartidas por separado ni generar otras distintas en cada nodo.

No ejecutar docker compose config sin --quiet en capturas: puede imprimir los secretos interpolados. Los .env se leen para interpolación; no se pasan completos a todos los contenedores.

## 9. Arrancar en orden, sin necesidad de reunirse

1. A inicia sus servicios centrales:

   ```bash
   cd ~/libreria/Nodo_A
   sudo bash up.sh
   ```

2. B y C pueden iniciar su paquete cuando A confirme que Eureka y Config Server están disponibles:

   ```bash
   cd ~/libreria/Nodo_B  # En C usar Nodo_C
   sudo bash up.sh
   sudo bash check.sh
   ```

3. A ejecuta check.sh y confirma que Eureka liste ms-auth-service, ms-cliente-service, ms-book-service y ms-venta-service con IP privadas alcanzables. Los servicios Java pueden tardar varios minutos y Eureka necesita tiempo para actualizar su registro.
4. Compartir por mensajes los resultados, las IP privadas y los errores sin secretos. Si A está disponible pero C aún no desplegó, el sistema completo no se considera listo.

No necesitan trabajar al mismo tiempo para crear y subir archivos. Para probar la integración, los tres Learner Labs y las EC2 deben estar encendidos a la vez.

## 10. Tu dominio de Spaceship y HTTPS

Cuando se conozca el dominio, elegir un único nombre público, por ejemplo libreria.TU-DOMINIO. El paquete permite cambiar PUBLIC_HOST sin recompilar Angular. No apuntar DNS a 10.10.1.10 ni a otra IP privada.

1. Confirma la IP pública estable o actual del nodo A.
2. Comprueba cuáles son los nameservers autoritativos del dominio. Si el DNS está alojado en otro proveedor, modifica la zona allí; comprar en Spaceship no garantiza que la zona activa esté en Spaceship.
3. En Spaceship abre **Advanced DNS Manager**, selecciona el dominio y entra a **DNS records → Custom records**.
4. Crea un registro A con host libreria y valor igual a la IPv4 pública de A. Para usar el dominio raíz, el host suele ser @. Guarda el registro.
5. No borres registros de correo MX/TXT. Si ya existe un A, AAAA o CNAME para ese MISMO nombre, revisa el conflicto antes de reemplazarlo. No publiques un AAAA si no configuraste IPv6. Un CAA existente debe permitir la autoridad usada para emitir el certificado.
6. Comprueba desde tu computadora:

   ```powershell
   Resolve-DnsName libreria.TU-DOMINIO -Type A
   ```

7. En A, configura PUBLIC_HOST=libreria.TU-DOMINIO en .env, sin protocolo ni rutas.
8. Comprueba que el Security Group de A permita TCP80 y443 desde Internet y que la subred tenga ruta a Internet Gateway.
9. En A ejecuta:

   ```bash
   sudo bash enable-https.sh
   curl -I https://libreria.TU-DOMINIO
   curl -fsS https://libreria.TU-DOMINIO/api/actuator/health
   ```

10. Abre https://libreria.TU-DOMINIO/login. No ignores alertas de certificados ni uses curl -k para dar la prueba por aprobada. Si aún no emite certificado, revisa DNS, puertos, registros CAA y logs de edge.

Caddy solicita y renueva certificados automáticamente; sus volúmenes caddy-data y caddy-config deben persistir. No se necesita comprar otro certificado para este diseño. Los cambios DNS no son instantáneos. Si cambia la IP pública tras reiniciar, actualiza el registro A y vuelve a verificarlo.

## 11. Credenciales y primer acceso

El coordinador consulta PRIVADO_NO_COMPARTIR_COMPLETO/ACCESO_APLICACION.md localmente. Usuarios iniciales: admin (administrador), cajero y almacen. Las contraseñas son aleatorias y diferentes de las locales anteriores.

Después del primer arranque correcto de B, desactivar BOOTSTRAP_ENABLED en su .env y recrear solo Auth:

```bash
sudo docker compose up -d --no-deps auth
```

No se cambia ninguna contraseña existente por editar .env. No borrar volúmenes para intentar corregir un login. No habilitar registro público de usuarios. En esta entrega las claves iniciales se generan, pero su acceso web en AWS queda pendiente de despliegue y validación.

## 12. Prueba de aceptación del sistema completo

Guardar fecha/hora, respuesta esperada y captura sin tokens:

1. HTTPS válido y frontend accesible desde el dominio.
2. Login de admin, cajero y almacen con las claves privadas del despliegue.
3. Sin token, GET /api/libros debe responder401. Cajero no debe administrar usuarios:403.
4. Admin crea un cliente y un producto con stock. Comprobar imagen subida, listado y persistencia.
5. Cajero agrega productos y registra una venta académica. Verificar stock, importe, método de pago y PDF.
6. Repetir la misma solicitud con el MISMO Idempotency-Key y datos: misma venta y un único descuento de stock. Usar solo datos de prueba; el sistema registra pagos manuales y no cobra al banco.
7. Confirmar en los registros que Venta en C consulta Auth en B y Book en C. Esta operación verifica la dependencia B–C que no cubría la prueba HTTP inicial.
8. Reiniciar contenedores sin borrar volúmenes y confirmar que usuarios, productos, clientes, ventas e imágenes continúan.
9. Si falla Book durante una venta, no marcarla completada a mano. Revisar las ventas PENDIENTES y reintentar después de recuperar conectividad. No hay un trabajador automático de conciliación.

La prueba de red de /health en8000 no sustituye estas pruebas. Las pruebas locales que acompañan el paquete no certifican un despliegue en AWS ni la seguridad de producción.

## 13. Diagnóstico

```bash
sudo docker compose ps
sudo docker compose logs --tail 80
sudo bash check.sh
```

Antes de compartir logs, revisarlos y retirar tokens, contraseñas y datos personales. No compartir docker inspect completo ni .env.

| Síntoma | Revisar |
|---|---|
| Timeout hacia otro nodo | Peering, ruta de ida/vuelta, subred efectiva, SG y ACL |
| Eureka muestra172.x de Docker en vez de la IP de EC2 | SERVICE_HOST / PRIVATE_IP del servicio |
| Config Server401 | CONFIG_SERVER_PASSWORD igual en A,B,C |
| MySQL Access denied | Contraseña usada al inicializar el volumen; editar .env no la rota |
| Gateway503 | Servicio no registrado, host privado incorrecto o EC2 remota apagada |
| Login401 | Credenciales del despliegue, no las del entorno local; JWT compartido |
| Reinicios/OOM | Memoria real de EC2 y límites; no usar una t3.micro para el nodo completo |
| Dominio deja de responder tras Stop/Start | IP pública/DNS, estado de EC2 y sesión de Learner Lab |

## 14. Respaldos, recuperación y cierre

En B y C ejecutar sudo bash backup.sh. Los respaldos contienen datos privados: conservarlos cifrados y fuera de la EC2. Respaldar B y C durante una ventana coordinada sin ventas nuevas, para evitar estados inconsistentes entre reservas y ventas. Conservar también imágenes, .env y configuración de forma privada.

Para restaurar, preparar un host limpio con la MISMA versión del paquete, recuperar .env, arrancar MySQL y cargar el SQL con mysql dentro del contenedor. En C restaurar también book-uploads; verificar reservas y ventas PENDIENTES antes de reabrir. No restaurar solo una base relacionada a un momento distinto sin reconciliación. El paquete no elimina datos ni automatiza restauraciones destructivas.

Al terminar la sesión: coordinar con el equipo, guardar evidencia y ejecutar sudo docker compose stop. Después detener EC2 y finalizar su Learner Lab. Los volúmenes, IPv4/Elastic IP y respaldos pueden seguir generando cargos según el recurso. No ejecutar docker compose down -v: borra las bases e imágenes persistentes. Si reutilizan la IP10.10.1.10 en una sesión futura, asegúrense de que el servidor de prueba8000 no siga abierto innecesariamente.

## 15. Mensaje para que ChatGPT guíe cada despliegue

Copiar este texto en el chat del integrante, sin pegar contraseñas:

> Estoy desplegando el nodo [A/B/C] de Librería Amanecer en AWS Academy Learner Lab. Tengo el paquete Nodo_[X].zip y mi .env privado guardado localmente. Sigue GUIA_DESPLIEGUE_AWS.md por etapas: verifica primero cuenta, región, VPC, subred, rutas y Security Groups; luego Ubuntu24.04 x86_64, memoria, Docker y archivos. No imprimas mis secretos, no cambies IAM, no borres volúmenes y no afirmes que funciona hasta verificar salud, Eureka y las operaciones integradas. Coordina las IP privadas con los otros integrantes. El dominio está [pendiente/indicar nombre] y solo el nodo A lo publica por HTTPS. Explica y registra cada cambio real antes de avanzar a la siguiente etapa.

## Referencias oficiales

- [Spaceship: registros DNS y Advanced DNS Manager](https://www.spaceship.com/knowledgebase/dns-records-types/).
- [Spaceship: propagación DNS](https://www.spaceship.com/knowledgebase/dns-propagation-guide/).
- [Caddy: HTTPS automático](https://caddyserver.com/docs/automatic-https).
- [Docker: instalación oficial en Ubuntu](https://docs.docker.com/engine/install/ubuntu/).
- [Docker Compose: interpolación de variables](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/).
- [AWS: limitaciones de VPC Peering](https://docs.aws.amazon.com/vpc/latest/peering/vpc-peering-basics.html).
