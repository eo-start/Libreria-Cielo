# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Optional end-to-end checks against the local Compose (Python standard library only)."""
import base64
import concurrent.futures
import http.client
import json
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'http://localhost:4200/api'
env = dict(line.split('=', 1) for line in (ROOT / '.env').read_text().splitlines() if '=' in line and not line.startswith('#'))
checks = []

def api(method, path, token=None, body=None, headers=None, raw=False):
    h = dict(headers or {})
    if token:
        h['Authorization'] = 'Bearer ' + token
    if body is not None and not isinstance(body, bytes):
        body = json.dumps(body).encode()
        h['Content-Type'] = 'application/json'
    req = urllib.request.Request(BASE + path, data=body, headers=h, method=method)
    try:
        with urllib.request.urlopen(req, timeout=12) as r:
            data = r.read()
            return r.status, data if raw else json.loads(data) if data else None
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def expect(condition, name):
    if not condition:
        raise AssertionError(name)
    checks.append(name)
    print('OK:', name, flush=True)

def login(name, password):
    code, result = api('POST', '/auth/login', body={'userName': name, 'password': password})
    assert code == 200, ('login', name, code)
    return result['token']

admin = login('admin', env['ADMIN_PASSWORD'])
cashier = login('cajero', env['CAJERO_PASSWORD'])
tag = uuid.uuid4().hex[:8]
code, customer = api('POST', '/cliente', admin, {'nombreCompleto': 'Verificacion ' + tag, 'dni': str(10000000 + int(tag, 16) % 80000000), 'telefono': '900000000', 'direccion': 'Datos ficticios de prueba'})
expect(code == 200, 'Crear cliente real en ms-cliente')
_, catalog = api('GET', '/libros', admin)
source = catalog[0]
png = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=')
boundary = '----libreria-' + tag
parts = []
fields = {'titulo': 'Prueba persistencia ' + tag, 'autor': 'Equipo', 'stock': '3', 'stockMinimo': '1', 'precio': '9.99', 'categoriaId': str(source['categoria']['id']), 'provedoresId': str(source['provedores']['id']), 'anio': '2026'}
for key, value in fields.items():
    parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{value}\r\n'.encode())
parts += [f'--{boundary}\r\nContent-Disposition: form-data; name="imagen"; filename="prueba.png"\r\nContent-Type: image/png\r\n\r\n'.encode() + png + b'\r\n', f'--{boundary}--\r\n'.encode()]
code, book = api('POST', '/libros', admin, b''.join(parts), {'Content-Type': 'multipart/form-data; boundary=' + boundary})
expect(code == 200, 'Crear producto con imagen PNG')
image_path = '/libros/uploads/' + book['imagenUrl']
expect(api('GET', image_path, raw=True) == (200, png), 'Imagen servida desde volumen persistente')
code, item = api('POST', '/carrito/agregar', admin, {'libroId': book['id'], 'cantidad': 1})
expect(code == 200, 'Crear carrito antes de reiniciar')
subprocess.run(['docker', 'compose', 'restart', 'auth', 'book', 'cliente', 'venta'], cwd=ROOT, check=True)
for attempt in range(75):
    try:
        c1, c = api('GET', '/cliente/' + str(customer['id']), admin)
        c2, b = api('GET', '/libros/' + str(book['id']), admin)
        c3, cart = api('GET', '/carrito/listar', admin)
        if c1 == c2 == c3 == 200:
            break
    except (urllib.error.URLError, TimeoutError, http.client.HTTPException):
        pass
    time.sleep(2)
else:
    raise AssertionError('Los servicios no se recuperaron después del reinicio')
expect(c['nombreCompleto'] == customer['nombreCompleto'], 'Cliente conservado después del reinicio')
expect(b['stock'] == 3, 'Producto y stock conservados después del reinicio')
expect(any(i['id'] == item['id'] for i in cart), 'Carrito conservado después del reinicio de Venta')
expect(api('GET', image_path, raw=True) == (200, png), 'Imagen conservada después del reinicio de Book')
operation = str(uuid.uuid4())
path = '/venta/realizar?' + urllib.parse.urlencode({'clienteNombre': customer['nombreCompleto'], 'clienteId': customer['id'], 'metodoPago': 'EFECTIVO'})
code, sale = api('POST', path, admin, headers={'Idempotency-Key': operation})
expect(code == 200 and sale['estado'] == 'COMPLETADA' and sale['clienteId'] == customer['id'], 'Venta relaciona cliente registrado después del reinicio')
expect(round(sale['totalConIgv'], 2) == 11.79, 'Importes calculados y redondeados por el backend')
expect(api('POST', '/venta/' + str(sale['id']) + '/reintentar', cashier)[0] == 403, 'Otro usuario no puede reintentar una venta ajena')
for token in (admin, cashier):
    expect(api('POST', '/carrito/agregar', token, {'libroId': book['id'], 'cantidad': 2})[0] == 200, 'Preparar comprador concurrente')

def buy(token):
    return api('POST', '/venta/realizar?clienteNombre=Concurrencia&metodoPago=EFECTIVO', token, headers={'Idempotency-Key': str(uuid.uuid4())})[0]

with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
    results = list(pool.map(buy, (admin, cashier)))
expect(sorted(results) == [200, 409], 'Solo un comprador obtiene las últimas dos unidades (MySQL real)')
expect(api('GET', '/libros/' + str(book['id']), admin)[1]['stock'] == 0, 'Stock final no negativo ni duplicado')
# Remove only the remaining test cart, preserving the persisted customer, product and sales as evidence.
for token in (admin, cashier):
    for row in api('GET', '/carrito/listar', token)[1]:
        if row['libroId'] == book['id']:
            api('DELETE', '/carrito/eliminar/' + str(row['id']), token)
report = {'resultado': 'OK', 'comprobaciones': checks, 'clienteId': customer['id'], 'productoId': book['id'], 'ventaId': sale['id']}
(ROOT / '.local' / 'verificacion-persistencia.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print('Verificación de persistencia y concurrencia completada.', flush=True)
