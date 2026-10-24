#!/bin/bash
# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
set -eu
[[ "$DB_PASSWORD" =~ ^[A-Za-z0-9_-]{16,}$ ]] || { echo 'DB_PASSWORD debe ser aleatoria, de al menos 16 caracteres URL-safe.' >&2; exit 1; }
for database in ms_auth ms_book ms_cliente ms_venta; do
 MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot <<SQL
CREATE DATABASE IF NOT EXISTS $database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$database'@'%' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $database.* TO '$database'@'%';
SQL
done
