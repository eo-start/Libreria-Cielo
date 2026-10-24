# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Exercise isolated QA bundles; writes only redacted results, never tokens/passwords."""
import argparse
import datetime
import json
from pathlib import Path
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

p = argparse.ArgumentParser()
p.add_argument('--credentials', required=True)
p.add_argument('--output', required=True)
p.add_argument('--base-url', default='http://127.0.0.1:18080/api')
args = p.parse_args()
env = dict(x.split('=', 1) for x in Path(args.credentials).read_text().splitlines() if '=' in x and not x.startswith('#'))
checks = []

def request(method, path, token=None, body=None, headers=None):
    h = dict(headers or {})
    if token: h['Authorization'] = 'Bearer ' + token
    if body is not None:
        h['Content-Type'] = 'application/json'
        body = json.dumps(body).encode()
    req = urllib.request.Request(args.base_url + path, data=body, method=method, headers=h)
    try:
        with urllib.request.urlopen(req, timeout=20) as response: return response.status, response.read()
    except urllib.error.HTTPError as exc: return exc.code, exc.read()

def expect(response, code, label):
    if response[0] != code: raise RuntimeError(label + ': HTTP' + str(response[0]) + ', esperado ' + str(code))
    checks.append({'check': label, 'status': 'PASS', 'http': code})
    return response[1]

result = {'at': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'environment': 'QA local aislado de tres Compose; no AWS', 'checks': checks}
try:
    tokens = {}
    for user, key in [('admin', 'ADMIN_PASSWORD'), ('cajero', 'CAJERO_PASSWORD'), ('almacen', 'ALMACEN_PASSWORD')]:
        response = None
        for attempt in range(30):
            response = request('POST', '/auth/login', body={'userName': user, 'password': env[key]})
            if response[0] == 200: break
            time.sleep(2)
        tokens[user] = json.loads(expect(response, 200, 'Login ' + user))['token']
    expect(request('GET', '/actuator/health'), 200, 'Gateway saludable')
    expect(request('GET', '/libros'), 401, 'Catalogo protegido sin token')
    expect(request('GET', '/auth/usuarios', tokens['cajero']), 403, 'Cajero no administra usuarios')
    expect(request('GET', '/auth/usuarios', tokens['admin']), 200, 'Administrador consulta usuarios')
    expect(request('GET', '/cliente', tokens['admin']), 200, 'Cliente accesible en nodo B')
    pending = request('GET', '/venta/pendiente', tokens['cajero'])
    assert pending[0] in (200, 204), 'Consulta de venta pendiente no disponible'
    checks.append({'check': 'Venta accesible en nodo C; sin pendiente puede devolver204', 'status': 'PASS', 'http': pending[0]})
    catalog = json.loads(expect(request('GET', '/libros', tokens['cajero']), 200, 'Book accesible en nodo C'))
    book = next(x for x in catalog if x['stock'] >= 2 and x['estado'] == 'ACTIVO')
    before = book['stock']
    expect(request('POST', '/carrito/agregar', tokens['cajero'], {'libroId': book['id'], 'cantidad': 2}), 200, 'Carrito: token de B aceptado en C')
    operation = str(uuid.uuid4())
    path = '/venta/realizar?' + urllib.parse.urlencode({'clienteNombre': 'Prueba paquetes AWS', 'metodoPago': 'TRANSFERENCIA', 'nroOperacion': 'QA-' + operation[:8]})
    h = {'Idempotency-Key': operation}
    sale = json.loads(expect(request('POST', path, tokens['cajero'], headers=h), 200, 'Checkout integrado Venta a Auth y Book'))
    assert sale['estado'] == 'COMPLETADA', 'Venta no completada'
    remaining = json.loads(expect(request('GET', '/libros/' + str(book['id']), tokens['cajero']), 200, 'Consultar stock tras venta'))['stock']
    assert remaining == before - 2, 'Descuento de stock incorrecto'
    repeated = json.loads(expect(request('POST', path, tokens['cajero'], headers=h), 200, 'Reintento idempotente'))
    assert repeated['id'] == sale['id'], 'Venta duplicada'
    after = json.loads(request('GET', '/libros/' + str(book['id']), tokens['cajero'])[1])['stock']
    assert after == remaining, 'Stock descontado dos veces'
    pdf = expect(request('GET', '/venta/' + str(sale['id']) + '/recibo', tokens['cajero']), 200, 'PDF generado')
    assert pdf.startswith(b'%PDF'), 'Contenido PDF invalido'
    result['sale'] = {'id': sale['id'], 'beforeStock': before, 'afterStock': after, 'duplicateStockDecrement': False}
    result['result'] = 'PASS'
except Exception as exc:
    result['result'] = 'FAIL'
    result['error'] = str(exc)
finally:
    Path(args.output).write_text(json.dumps(result, indent=2, ensure_ascii=False), encoding='utf-8')
print(json.dumps({'result': result['result'], 'checks': len(checks), 'error': result.get('error')}, ensure_ascii=False))
raise SystemExit(0 if result['result'] == 'PASS' else 1)
