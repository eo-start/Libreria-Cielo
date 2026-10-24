#!/usr/bin/env bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -euo pipefail
cd "$(dirname "$0")"
node=$(python3 -c 'import json;print(json.load(open("NODE.json"))["node"])')
private_ip=$(python3 -c 'print(dict(x.split("=",1) for x in open(".env").read().splitlines() if x and not x.startswith("#") and "=" in x)["PRIVATE_IP"])')
docker compose ps
case "$node" in
 A) curl -fsS --max-time 15 http://127.0.0.1:8080/api/actuator/health; echo;
    curl -fsS --max-time 15 -H 'Accept: application/json' "http://$private_ip:8090/eureka/apps"; echo;;
 B) for port in 8081 8083; do curl -fsS --max-time 15 "http://$private_ip:$port/actuator/health"; echo; done;;
 C) for port in 8082 8084; do curl -fsS --max-time 15 "http://$private_ip:$port/actuator/health"; echo; done;;
esac
echo 'Comprobacion de salud terminada. La aceptacion completa requiere login y una venta integrada.'
