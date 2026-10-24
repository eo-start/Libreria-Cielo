#!/usr/bin/env bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -euo pipefail
cd "$(dirname "$0")"
python3 preflight.py --public
echo 'Requiere DNS apuntando a esta EC2 y TCP80/443 accesibles. No modifica DNS ni Security Groups.'
docker compose --profile public up -d edge
docker compose logs --tail 40 edge
