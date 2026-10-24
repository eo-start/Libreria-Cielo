#!/usr/bin/env python3
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Check a prepared node without printing credentials or modifying infrastructure."""
import argparse
import ipaddress
import json
import os
from pathlib import Path
import re
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('--public', action='store_true')
args = parser.parse_args()
root = Path(__file__).resolve().parent
os.chdir(root)
node = json.loads((root / 'NODE.json').read_text())['node']
env_file = root / '.env'
if not env_file.exists():
    raise SystemExit('Falta .env. Solicita al coordinador el archivo PRIVADO de este nodo.')
values = dict(line.split('=', 1) for line in env_file.read_text().splitlines()
              if line.strip() and not line.lstrip().startswith('#') and '=' in line)
required = ['PRIVATE_IP', 'NODE_A_IP', 'JWT_SECRET', 'CONFIG_SERVER_PASSWORD']
if node in ('B', 'C'):
    required += ['MYSQL_ROOT_PASSWORD']
    required += ['AUTH_DB_PASSWORD', 'CLIENTE_DB_PASSWORD', 'ADMIN_PASSWORD',
                 'CAJERO_PASSWORD', 'ALMACEN_PASSWORD'] if node == 'B' else ['BOOK_DB_PASSWORD', 'VENTA_DB_PASSWORD']
for key in required:
    value = values.get(key, '')
    if not value or 'REEMPLAZAR' in value or 'CHANGE_ME' in value:
        raise SystemExit('Falta configurar ' + key)
    if 'PASSWORD' in key and not re.fullmatch(r'[A-Za-z0-9_-]{16,}', value):
        raise SystemExit(key + ' debe tener al menos 16 caracteres URL-safe.')
if len(values['JWT_SECRET'].encode()) < 32:
    raise SystemExit('JWT_SECRET debe tener al menos 32 bytes.')
private_ranges = [ipaddress.ip_network(x) for x in ['10.0.0.0/8', '172.16.0.0/12', '192.168.0.0/16']]
for key in ['PRIVATE_IP', 'NODE_A_IP']:
    address = ipaddress.ip_address(values[key])
    if not any(address in network for network in private_ranges):
        raise SystemExit(key + ' debe ser una IPv4 privada real de la EC2.')
if os.name == 'posix':
    addresses = json.loads(subprocess.check_output(['ip', '-j', 'address'], text=True))
    local = {item['local'] for interface in addresses for item in interface.get('addr_info', [])}
    if values['PRIVATE_IP'] not in local:
        raise SystemExit('PRIVATE_IP no pertenece a esta EC2. Corrige .env antes de iniciar.')
    if env_file.stat().st_mode & 0o077:
        raise SystemExit('Protege el archivo: chmod 600 .env')
if args.public:
    if node != 'A':
        raise SystemExit('Solo el nodo A publica el dominio.')
    domain = values.get('PUBLIC_HOST', '')
    if not re.fullmatch(r'[a-zA-Z0-9](?:[a-zA-Z0-9.-]*[a-zA-Z0-9])?', domain) or '.' not in domain or domain.endswith(('.invalid', '.example', '.test')):
        raise SystemExit('Configura PUBLIC_HOST con tu dominio o subdominio real, sin https:// ni rutas.')
subprocess.run(['docker', 'compose', 'config', '--quiet'], check=True)
print('Configuracion del nodo ' + node + ' valida. No se imprimieron secretos.')
