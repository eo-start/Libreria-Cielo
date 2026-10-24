#!/usr/bin/env bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -euo pipefail
cd "$(dirname "$0")"
python3 preflight.py
node=$(python3 -c 'import json;print(json.load(open("NODE.json"))["node"])')
case "$node" in
 A)
  docker compose up -d --build --wait --wait-timeout 300 registry config
  docker compose up -d --build gateway frontend
  ;;
 B)
  docker compose up -d --wait --wait-timeout 300 mysql
  docker compose up -d --build auth cliente
  ;;
 C)
  docker compose up -d --wait --wait-timeout 300 mysql
  docker compose up -d --build book venta
  ;;
 *) echo 'Nodo desconocido' >&2; exit 1;;
esac
docker compose ps
echo 'Contenedores iniciados. Ejecuta check.sh; el arranque de Java y Eureka puede tardar varios minutos.'
