#!/usr/bin/env bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -euo pipefail
if [[ $EUID != 0 ]]; then echo 'Ejecuta con sudo bash install-docker-ubuntu.sh'; exit 1; fi
. /etc/os-release
if [[ "$ID" != ubuntu || "$VERSION_ID" != '24.04' ]]; then echo 'Este instalador esta preparado para Ubuntu24.04. Consulta la guia oficial para otra version.'; exit 1; fi
if command -v docker >/dev/null; then
 docker version
 docker compose version
 echo 'Docker ya existe. No se reemplazo la instalacion.'
 exit 0
fi
apt-get update
apt-get install -y ca-certificates curl python3
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
cat > /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: ${UBUNTU_CODENAME:-$VERSION_CODENAME}
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker
docker compose version
echo 'Usa sudo para Docker. No se agregaron usuarios al grupo docker.'
