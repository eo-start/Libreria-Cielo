# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Prepare three independent deployment bundles; never deploys AWS resources.

Requires Python3, PyYAML, Docker, successful Maven package and a freshly built
frontend image named libreria-frontend:aws-preparado. Credentials are generated
outside shareable archives and preserved on subsequent runs.
"""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import secrets
import shutil
import subprocess
import zipfile
import yaml

ROOT = Path(__file__).resolve().parents[1]
TEMPLATES = ROOT / 'deploy' / 'aws-team'
parser = argparse.ArgumentParser()
parser.add_argument('--output', required=True)
args = parser.parse_args()
OUT = Path(args.output).resolve()
OUT.mkdir(parents=True, exist_ok=True)
PRIVATE = OUT / 'PRIVADO_NO_COMPARTIR_COMPLETO'
PRIVATE.mkdir(exist_ok=True)
VERSION = 'academia-20261023'
MODULES = {'A': ['ms-registry-server', 'ms-config-server', 'ms-gateway-service'],
           'B': ['ms-auth', 'ms-cliente'], 'C': ['ms-book', 'ms-venta']}

def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8', newline='\n')

def image(tag):
    try:
        obj = json.loads(subprocess.check_output(['docker', 'image', 'inspect', tag]))[0]
        return obj['RepoDigests'][0]
    except (subprocess.CalledProcessError, IndexError):
        subprocess.run(['docker', 'pull', tag], check=True, stdout=subprocess.DEVNULL)
        return json.loads(subprocess.check_output(['docker', 'image', 'inspect', tag]))[0]['RepoDigests'][0]

images = {x: image(x) for x in ['eclipse-temurin:17-jre', 'mysql:8.4', 'nginx:1.28-alpine', 'caddy:2-alpine']}
secret_file = PRIVATE / 'secrets.json'
if secret_file.exists():
    values = json.loads(secret_file.read_text())
else:
    values = {key: secrets.token_urlsafe(32) for key in ['JWT_SECRET', 'CONFIG_SERVER_PASSWORD',
        'B_MYSQL_ROOT_PASSWORD', 'C_MYSQL_ROOT_PASSWORD', 'AUTH_DB_PASSWORD', 'CLIENTE_DB_PASSWORD',
        'BOOK_DB_PASSWORD', 'VENTA_DB_PASSWORD', 'ADMIN_PASSWORD', 'CAJERO_PASSWORD', 'ALMACEN_PASSWORD']}
    write(secret_file, json.dumps(values, indent=2))
secret_bytes = [v.encode() for v in values.values()]
local_env = ROOT / '.env'
if local_env.exists():
    secret_bytes += [x.split('=', 1)[1].encode() for x in local_env.read_text().splitlines()
                     if '=' in x and not x.startswith('#') and len(x.split('=', 1)[1]) >= 16]

def java_service(module, memory=640, heap=320, environment=None):
    return {'image': 'libreria-aws/' + module + ':' + VERSION,
            'build': {'context': '.', 'dockerfile': 'Dockerfile.java', 'args': {'MODULE': module}},
            'restart': 'unless-stopped', 'mem_limit': str(memory) + 'm',
            'environment': {'JAVA_TOOL_OPTIONS': '-Xms64m -Xmx' + str(heap) + 'm -XX:+ExitOnOutOfMemoryError',
                            **(environment or {})}}

def backend(module, port, domain):
    service = java_service(module, environment={
        'SERVICE_HOST': '${PRIVATE_IP:?Completa_PRIVATE_IP}',
        'EUREKA_URI': 'http://${NODE_A_IP:?Completa_NODE_A_IP}:8090/eureka/',
        'SPRING_CLOUD_CONFIG_ENABLED': 'true',
        'SPRING_CONFIG_IMPORT': 'configserver:http://${NODE_A_IP}:7071',
        'SPRING_CLOUD_CONFIG_USERNAME': 'root',
        'SPRING_CLOUD_CONFIG_PASSWORD': '${CONFIG_SERVER_PASSWORD:?Falta_CONFIG_SERVER_PASSWORD}',
        'JWT_SECRET': '${JWT_SECRET:?Falta_JWT_SECRET}',
        'DB_URL': 'jdbc:mysql://mysql:3306/ms_' + domain + '?serverTimezone=UTC',
        'DB_USERNAME': 'ms_' + domain,
        'DB_PASSWORD': '${' + domain.upper() + '_DB_PASSWORD:?Falta_clave_DB}',
        'SEED_DEMO_DATA': '${SEED_DEMO_DATA:-false}',
    })
    service['ports'] = ['${PRIVATE_IP}:' + str(port) + ':' + str(port)]
    service['depends_on'] = {'mysql': {'condition': 'service_healthy'}}
    return service

def mysql_service(domains):
    return {'image': images['mysql:8.4'], 'restart': 'unless-stopped', 'mem_limit': '900m',
        'environment': {'MYSQL_ROOT_PASSWORD': '${MYSQL_ROOT_PASSWORD:?Falta_clave_root}',
                        **{d.upper() + '_DB_PASSWORD': '${' + d.upper() + '_DB_PASSWORD}' for d in domains}},
        'volumes': ['mysql-data:/var/lib/mysql', './init-db.sh:/docker-entrypoint-initdb.d/01-databases.sh:ro'],
        'command': ['--innodb-buffer-pool-size=128M', '--max-connections=40'],
        'healthcheck': {'test': ['CMD-SHELL', 'MYSQL_PWD="$$MYSQL_ROOT_PASSWORD" mysqladmin ping -h 127.0.0.1 -uroot --silent'],
                        'interval': '5s', 'timeout': '4s', 'retries': 50}}

def source_copy(source, destination):
    def ignore(directory, names):
        return [n for n in names if n in ['target', 'node_modules', '.git', '.local', 'dist', '.angular', 'uploads', '__pycache__']
                or n == '.env' or (n.startswith('.env.') and n != '.env.example')
                or n.endswith(('.pem', '.key', '.log', '.zip', '.rar'))]
    shutil.copytree(source, destination, dirs_exist_ok=True, ignore=ignore)

summary = {'version': VERSION, 'preparedAt': datetime.datetime.now(datetime.timezone.utc).isoformat(),
           'domain': 'PENDIENTE', 'images': images, 'packages': {}, 'deployedToAWS': False}
for node, modules in MODULES.items():
    folder = OUT / ('Nodo_' + node)
    folder.mkdir(exist_ok=True)
    for template in TEMPLATES.iterdir():
        if template.is_file(): shutil.copyfile(template, folder / template.name)
    write(folder / 'Dockerfile.java', (folder / 'Dockerfile.java').read_text().replace('eclipse-temurin:17-jre', images['eclipse-temurin:17-jre']))
    write(folder / 'Dockerfile.frontend', (folder / 'Dockerfile.frontend').read_text().replace('nginx:1.28-alpine', images['nginx:1.28-alpine']))
    shutil.copyfile(ROOT / 'docker/java-security.properties', folder / 'java-security.properties')
    (folder / 'artifacts').mkdir(exist_ok=True)
    for module in modules:
        jars = list((ROOT / module / 'target').glob('*.jar'))
        if len(jars) != 1: raise RuntimeError('Se esperaba un JAR compilado para ' + module)
        shutil.copyfile(jars[0], folder / 'artifacts' / (module + '.jar'))
        source_copy(ROOT / module, folder / 'source' / module)
    compose = {'name': 'libreria-' + node.lower(), 'services': {}, 'volumes': {}}
    services = compose['services']
    env = {'PRIVATE_IP': '10.10.1.10' if node == 'A' else '172.31.28.189' if node == 'B' else '10.20.1.10',
           'NODE_A_IP': '10.10.1.10', 'JWT_SECRET': values['JWT_SECRET'],
           'CONFIG_SERVER_PASSWORD': values['CONFIG_SERVER_PASSWORD']}
    if node == 'A':
        services['registry'] = java_service('ms-registry-server', 512, 256)
        services['registry']['ports'] = ['${PRIVATE_IP}:8090:8090']
        services['registry']['healthcheck'] = {'test': ['CMD-SHELL', 'bash -c "</dev/tcp/localhost/8090"'], 'interval': '5s', 'timeout': '3s', 'retries': 50}
        services['config'] = java_service('ms-config-server', 384, 192, {
            'CONFIG_SERVER_PASSWORD': '${CONFIG_SERVER_PASSWORD}',
            'SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS': 'file:/config-data'})
        services['config']['ports'] = ['${PRIVATE_IP}:7071:7071']
        services['config']['volumes'] = ['./config-data:/config-data:ro']
        services['config']['healthcheck'] = {'test': ['CMD-SHELL', 'bash -c "</dev/tcp/localhost/7071"'], 'interval': '5s', 'timeout': '3s', 'retries': 50}
        services['gateway'] = java_service('ms-gateway-service', 512, 256, {
            'JWT_SECRET': '${JWT_SECRET}', 'SERVICE_HOST': '${PRIVATE_IP}',
            'EUREKA_URI': 'http://registry:8090/eureka/'})
        services['gateway']['depends_on'] = {'registry': {'condition': 'service_healthy'}}
        services['frontend'] = {'image': 'libreria-aws/frontend:' + VERSION,
            'build': {'context': '.', 'dockerfile': 'Dockerfile.frontend'},
            'restart': 'unless-stopped', 'mem_limit': '96m', 'ports': ['127.0.0.1:8080:80'],
            'environment': {'GATEWAY_HOST': 'gateway'}, 'depends_on': ['gateway']}
        services['edge'] = {'image': images['caddy:2-alpine'], 'profiles': ['public'],
            'restart': 'unless-stopped', 'mem_limit': '128m', 'environment': {'PUBLIC_HOST': '${PUBLIC_HOST:-libreria.example.invalid}'},
            'ports': ['80:80', '443:443'], 'volumes': ['./Caddyfile:/etc/caddy/Caddyfile:ro', 'caddy-data:/data', 'caddy-config:/config'],
            'depends_on': ['frontend']}
        compose['volumes'] = {'caddy-data': {}, 'caddy-config': {}}
        env['PUBLIC_HOST'] = 'libreria.example.invalid'
        source_copy(ROOT / 'config-data', folder / 'config-data')
        source_copy(ROOT / 'libreria-amanecer', folder / 'source/libreria-amanecer')
        shutil.copyfile(ROOT / 'libreria-amanecer/nginx.conf', folder / 'nginx.conf')
        container = subprocess.check_output(['docker', 'create', 'libreria-frontend:aws-preparado'], text=True).strip()
        try:
            (folder / 'frontend-dist').mkdir(exist_ok=True)
            subprocess.run(['docker', 'cp', container + ':/usr/share/nginx/html/.', str(folder / 'frontend-dist')], check=True)
        finally:
            subprocess.run(['docker', 'rm', container], check=True, stdout=subprocess.DEVNULL)
    else:
        domains = ['auth', 'cliente'] if node == 'B' else ['book', 'venta']
        services['mysql'] = mysql_service(domains)
        compose['volumes']['mysql-data'] = {}
        for domain, port in [('auth', 8081), ('cliente', 8083)] if node == 'B' else [('book', 8082), ('venta', 8084)]:
            services[domain] = backend('ms-' + domain, port, domain)
        env['MYSQL_ROOT_PASSWORD'] = values[node + '_MYSQL_ROOT_PASSWORD']
        env['SEED_DEMO_DATA'] = 'false'
        init = '#!/bin/bash\nset -euo pipefail\n'
        for domain in domains:
            key = domain.upper() + '_DB_PASSWORD'
            env[key] = values[key]
            init += ('[[ "$' + key + '" =~ ^[A-Za-z0-9_-]{16,}$ ]] || { echo "Clave invalida para ' + domain + '" >&2; exit 1; }\n'
                'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot <<SQL\n'
                'CREATE DATABASE IF NOT EXISTS ms_' + domain + ' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n'
                "CREATE USER IF NOT EXISTS 'ms_" + domain + "'@'%' IDENTIFIED BY '$" + key + "';\n"
                'GRANT ALL PRIVILEGES ON ms_' + domain + ".* TO 'ms_" + domain + "'@'%';\nSQL\n")
        write(folder / 'init-db.sh', init)
        if node == 'B':
            env['BOOTSTRAP_ENABLED'] = 'true'
            for key in ['ADMIN_PASSWORD', 'CAJERO_PASSWORD', 'ALMACEN_PASSWORD']:
                env[key] = values[key]
                services['auth']['environment'][key] = '${' + key + '}'
            services['auth']['environment']['BOOTSTRAP_ENABLED'] = '${BOOTSTRAP_ENABLED:-false}'
        else:
            compose['volumes']['book-uploads'] = {}
            services['book']['volumes'] = ['book-uploads:/app/uploads']
            services['book']['environment']['UPLOADS_DIR'] = '/app/uploads'
    write(folder / 'compose.yml', yaml.safe_dump(compose, sort_keys=False, allow_unicode=True))
    write(folder / 'NODE.json', json.dumps({'node': node, 'version': VERSION, 'modules': modules}, indent=2))
    write(folder / '.env.example', '\n'.join(k + '=' + ('REEMPLAZAR_SECRETO_PRIVADO' if ('PASSWORD' in k or k == 'JWT_SECRET') else v) for k, v in env.items()) + '\n')
    write(PRIVATE / ('Nodo_' + node + '.env'), '\n'.join(k + '=' + v for k, v in env.items()) + '\n')
    write(folder / '.dockerignore', '.env\n.env.*\nsource/\nbackups/\n.git/\n*.log\n')
    write(folder / 'LEEME.md', '# Nodo ' + node + '\n\n'
        'Lee GUIA_DESPLIEGUE_AWS.md antes de crear recursos. Este paquete no contiene credenciales reales.\n'
        'Solicita al coordinador UNICAMENTE Nodo_' + node + '.env por canal privado, guardalo como .env y ejecuta chmod 600 .env.\n'
        'Confirma PRIVATE_IP y NODE_A_IP. La IP del nodo C es propuesta, no una EC2 existente.\n\n'
        'Requiere Ubuntu24.04 x86_64, Docker Compose v2, aproximadamente4GiB de RAM y20GiB de disco.\n'
        'Despues: chmod +x *.sh; sudo bash up.sh. No publiques secretos ni ejecutes down -v.\n'
        'El codigo de los servicios esta en source/; los JAR listos estan en artifacts/.\n')
    summary['packages'][node] = {'folder': str(folder), 'modules': modules}

write(PRIVATE / 'ACCESO_APLICACION.md', '# Credenciales privadas para el futuro despliegue\n\n'
      'NO compartir este archivo ni esta carpeta completa con los integrantes. No son credenciales de AWS ni de Spaceship.\n'
      'Todavia no se han instalado ni verificado en AWS. Se crearan al primer arranque de Auth con BOOTSTRAP_ENABLED=true y una base nueva.\n\n'
      '| Usuario | Rol | Clave inicial |\n|---|---|---|\n' +
      '\n'.join('|' + user + '|' + role + '|' + values[key] + '|' for user, role, key in [
          ('admin', 'Administrador', 'ADMIN_PASSWORD'), ('cajero', 'Cajero', 'CAJERO_PASSWORD'), ('almacen', 'Almacen', 'ALMACEN_PASSWORD')]) +
      '\n\nCada integrante recibe SOLO su Nodo_X.env por canal privado. Los tres necesitan la misma JWT_SECRET y CONFIG_SERVER_PASSWORD.\n'
      'Las contraseñas MySQL son diferentes por nodo y por dominio. Si ya existe la base, cambiar .env no cambia la contraseña almacenada.\n'
      'Despues del primer arranque correcto de Auth, cambia BOOTSTRAP_ENABLED=false en el .env de B y recrea auth.\n')
write(PRIVATE / 'LEEME.md', '# Carpeta privada\n\n'
      'Conservar fuera de Git, de los ZIP compartidos y de capturas. No subir a un bucket publico ni pegar en un chat compartido.\n'
      'Nodo_A.env es del coordinador; entregar Nodo_B.env solo al integrante B y Nodo_C.env solo al integrante C por un canal privado.\n'
      'Los ejemplos de IP se deben confirmar. PUBLIC_HOST queda pendiente del dominio real.\n')
for path in PRIVATE.iterdir():
    if path.is_file():
        try: os.chmod(path, 0o600)
        except OSError: pass
write(OUT / 'PREPARACION.json', json.dumps(summary, indent=2, ensure_ascii=False))
shutil.copyfile(TEMPLATES / 'GUIA_DESPLIEGUE_AWS.md', OUT / 'GUIA_DESPLIEGUE_AWS.md')
write(OUT / 'EMPEZAR_AQUI.md', '''# Entrega AWS de Libreria Amanecer

## Que subir y que entregar

| Persona | Archivo compartible | Archivo privado, enviado por separado |
|---|---|---|
| Tu, coordinador A | Nodo_A.zip | PRIVADO_NO_COMPARTIR_COMPLETO/Nodo_A.env |
| Companero1 B | Nodo_B.zip | PRIVADO_NO_COMPARTIR_COMPLETO/Nodo_B.env |
| Companero2 C | Nodo_C.zip | PRIVADO_NO_COMPARTIR_COMPLETO/Nodo_C.env |

1. Lee GUIA_DESPLIEGUE_AWS.md: inventario, red, EC2, subida, DNS de Spaceship y prompts para que ChatGPT guie a cada integrante.
2. Decide las EC2 definitivas: Ubuntu24.04 x86_64 y aproximadamente4GiB de RAM por nodo. Las EC2 de prueba no se consideran hosts definitivos.
3. Confirma las IP privadas y actualiza PRIVATE_IP y NODE_A_IP en los .env. La IP de C es propuesta, no una maquina creada.
4. Conecta C con A y B. Venta en C necesita Auth en B; se requiere tambien peering B-C.
5. Sube tu paquete y tu .env a tu EC2. Cada companero hace lo mismo con el suyo.
6. Inicia A, luego B y C; prueba Eureka y las operaciones integradas. No necesitan reunion presencial, pero los tres labs deben estar activos para la prueba final.
7. Completa el dominio real en PUBLIC_HOST del nodo A y publica DNS/HTTPS siguiendo la guia. No se modifico Spaceship durante esta preparacion.
8. Guarda evidencia, respalda datos y detengan los hosts al terminar.

Consulta localmente PRIVADO_NO_COMPARTIR_COMPLETO/ACCESO_APLICACION.md para admin, cajero y almacen. Son claves nuevas del despliegue, no de AWS, Spaceship o la instalacion local anterior. Los usuarios se crean cuando Auth arranca por primera vez con una base nueva.

No compartas la carpeta privada completa, no la subas a Git ni la adjuntes al informe. Cada integrante recibe solamente su .env por un canal privado. Los ZIP no contienen secretos.

Los paquetes se prepararon y verificaron localmente. No se desplego el sistema distribuido en AWS ni se emitio un certificado para tu dominio. Consulta VERIFICACION_PREPARACION.json para conocer las pruebas realmente ejecutadas.
''')
print(json.dumps({'output': str(OUT), 'nodes': list(MODULES), 'credentialsPrinted': False, 'deployed': False}))
