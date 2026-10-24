# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN --mount=type=secret,id=ca_bundle,required=false if [ -f /run/secrets/ca_bundle ]; then export NODE_EXTRA_CA_CERTS=/run/secrets/ca_bundle; fi; npm ci --legacy-peer-deps
COPY . .
RUN --mount=type=secret,id=ca_bundle,required=false if [ -f /run/secrets/ca_bundle ]; then export NODE_EXTRA_CA_CERTS=/run/secrets/ca_bundle; fi; npm run build
FROM nginx:1.28-alpine
COPY --from=build /app/dist/libreria-amanecer/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
ENV GATEWAY_HOST=gateway
EXPOSE 80
