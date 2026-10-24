# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
"""Export current source, without Git history, local credentials or build output."""
import json
import subprocess
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
paths = subprocess.check_output(
    ['git', 'ls-files', '-z', '--cached', '--others', '--exclude-standard'], cwd=root
).decode('utf-8').split('\0')
secrets = []
if (root / '.env').exists():
    for line in (root / '.env').read_text().splitlines():
        if '=' in line and not line.startswith('#'):
            value = line.split('=', 1)[1]
            if len(value) >= 16:
                secrets.append(value.encode())
files = []
for name in sorted(set(filter(None, paths))):
    path = Path(name)
    if any(part in {'.git', '.local', 'target', 'node_modules', 'dist', '.angular', '__pycache__'} for part in path.parts):
        continue
    if path.name.startswith('.env') and path.name != '.env.example':
        continue
    if path.suffix.lower() in {'.pem', '.key', '.log', '.rar', '.zip'}:
        continue
    source = root / path
    if not source.is_file():
        continue
    content = source.read_bytes()
    if any(secret in content for secret in secrets):
        raise RuntimeError('No se exportó: se encontró un secreto local en ' + name)
    files.append((name, content))
output = root / '.local' / 'Sistema-Venta-Libreria-equipo.zip'
output.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
    for name, content in files:
        archive.writestr(name, content)
print(json.dumps({'archivo': str(output), 'archivos': len(files), 'bytes': output.stat().st_size, 'secretos_locales': False}))
