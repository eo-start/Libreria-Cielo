#!/usr/bin/env bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -euo pipefail
umask 077
cd "$(dirname "$0")"
node=$(python3 -c 'import json;print(json.load(open("NODE.json"))["node"])')
if [[ "$node" == A ]]; then echo 'Nodo A: conservar .env, config-data y volumen de certificados Caddy con acceso privado.'; exit 0; fi
mkdir -p backups
stamp=$(date -u +%Y%m%dT%H%M%SZ)
if [[ "$node" == B ]]; then databases='ms_auth ms_cliente'; else databases='ms_book ms_venta'; fi
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot --single-transaction --no-tablespaces --set-gtid-purged=OFF --databases '"$databases" | gzip > "backups/$node-$stamp.sql.gz"
if [[ "$node" == C ]]; then
 docker compose run --rm --no-deps --entrypoint tar book -C /app/uploads -czf - . > "backups/uploads-$stamp.tar.gz"
fi
echo 'Copia creada en backups/. Contiene datos privados. Guarda otra copia fuera de la EC2.'
