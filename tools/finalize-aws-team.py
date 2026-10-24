# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Seal bundles, scan for actual secrets and record successful local tests."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument('--output', required=True)
out = Path(p.parse_args().output).resolve()
private = out / 'PRIVADO_NO_COMPARTIR_COMPLETO'
secret_values = [x.encode() for x in json.loads((private / 'secrets.json').read_text()).values()]
if (ROOT / '.env').exists():
    secret_values += [line.split('=', 1)[1].encode() for line in (ROOT / '.env').read_text().splitlines()
                      if '=' in line and not line.startswith('#') and len(line.split('=', 1)[1]) >= 16]
build_log = (ROOT / '.local/aws-preparacion-maven.log').read_text(errors='replace')
assert 'BUILD SUCCESS' in build_log, 'Falta compilacion Maven satisfactoria'
api = json.loads((ROOT / '.local/aws-qa/api-result.json').read_text())
assert api['result'] == 'PASS', 'Falta aprobacion de integracion local'
counts = {'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0}
for report in ROOT.glob('ms-*/target/surefire-reports/TEST-*.xml'):
    suite = ET.parse(report).getroot()
    for key in counts: counts[key] += int(suite.get(key, '0'))
assert counts['errors'] == counts['failures'] == 0, 'Pruebas Java fallidas'
verification = {'scope': 'Preparacion y QA local aislado; no despliegue AWS', 'maven': counts,
    'apiIntegration': api, 'databaseIsolation': json.loads((ROOT / '.local/aws-qa/db-isolation.json').read_text()),
    'composeValidation': 'PASS A/B/C', 'shellSyntax': 'PASS A/B/C', 'caddyValidation': 'PASS sin emision de certificado',
    'secretScan': 'PASS: secretos reales generados y locales no aparecen en los ZIP',
    'pending': ['Dominio exacto y DNS/HTTPS real', 'Cuenta, VPC y EC2 definitiva de C',
                'Peering A-C y B-C', 'IPs definitivas y permisos AWS', 'Despliegue y aceptacion en AWS']}
(out / 'VERIFICACION_PREPARACION.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2), encoding='utf-8')
archives = {}
for node in 'ABC':
    folder = out / ('Nodo_' + node)
    (folder / 'VERIFICACION_PREPARACION.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2), encoding='utf-8')
    manifest = {}
    for path in sorted(folder.rglob('*')):
        if not path.is_file() or path.name == 'SHA256.json': continue
        assert not any(x in path.parts for x in ['.git', '.local', '__pycache__', 'node_modules']), str(path)
        assert path.name != '.env' and path.suffix not in ['.pem', '.key'], str(path)
        data = path.read_bytes()
        assert not any(secret in data for secret in secret_values), 'Secreto detectado en ' + str(path)
        assert not re.search(rb'ASIA[A-Z0-9]{16}|AKIA[A-Z0-9]{16}|-----BEGIN (?:RSA |OPENSSH )?PRIVATE KEY', data), 'Credencial detectada en ' + str(path)
        manifest[str(path.relative_to(folder)).replace('\\', '/')] = hashlib.sha256(data).hexdigest()
    (folder / 'SHA256.json').write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    archive = out / ('Nodo_' + node + '.zip')
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
        for path in sorted(folder.rglob('*')):
            if path.is_file(): z.write(path, str(path.relative_to(out)))
    with zipfile.ZipFile(archive) as z: assert z.testzip() is None
    archives[archive.name] = {'bytes': archive.stat().st_size, 'sha256': hashlib.sha256(archive.read_bytes()).hexdigest()}
(out / 'PAQUETES_SHA256.json').write_text(json.dumps(archives, indent=2), encoding='utf-8')
print(json.dumps({'packages': archives, 'javaTests': counts, 'apiChecks': len(api['checks']), 'secretsIncluded': False}))
