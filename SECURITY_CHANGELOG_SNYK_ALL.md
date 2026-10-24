# Evidencia consolidada de remediacion Snyk

Fecha: 2026-04-24
Proyecto: `Sistema-Venta-Libreria`
Responsable del cambio: Codex
Objetivo: consolidar unicamente los cambios aplicados para retirar errores criticos reportados por Snyk

## Microservicios corregidos

- `ms-registry-server`
- `ms-gateway-service`
- `ms-auth`
- `ms-config-server`

## Vulnerabilidades corregidas

### Registry Server

- `CVE-2025-12383` en `org.glassfish.jersey.core:jersey-client@3.1.9`
- `CVE-2024-50379` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2024-56337` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2025-66614` en `org.apache.tomcat.embed:tomcat-embed-core@10.1.33`
- `CVE-2025-22228` en `org.springframework.security:spring-security-crypto@6.2.8`

### Gateway

- `CVE-2025-41243` en `org.springframework.cloud:spring-cloud-gateway-server@4.1.6`

### Auth

- `CVE-2026-22732` en `org.springframework.security:spring-security-web@6.3.8`

### Config Server

- `CVE-2026-22732` en `org.springframework.security:spring-security-web@6.3.8`

## Cambios aplicados

### 1. ms-registry-server

Archivo: `ms-registry-server/pom.xml`

Antes:
```xml
<version>3.2.12</version>
<spring-cloud.version>2023.0.4</spring-cloud.version>
<tomcat.version>10.1.33</tomcat.version>
<spring-security.version>6.2.8</spring-security.version>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
    <version>4.2.3</version>
</dependency>
```

Despues:
```xml
<version>3.3.7</version>
<spring-cloud.version>2023.0.6</spring-cloud.version>
<tomcat.version>10.1.50</tomcat.version>
<spring-security.version>6.3.8</spring-security.version>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

Cambio adicional:
```xml
<dependency>
    <groupId>org.glassfish.jersey.core</groupId>
    <artifactId>jersey-client</artifactId>
    <version>3.1.10</version>
</dependency>
```

Motivo:
Actualizar el stack de Eureka y forzar versiones corregidas de Tomcat, Spring Security y Jersey para retirar los criticos de Snyk.

### 2. ms-gateway-service

Archivo: `ms-gateway-service/pom.xml`

Antes:
```xml
<version>3.3.7</version>
<spring-cloud.version>2023.0.4</spring-cloud.version>
```

Despues:
```xml
<version>3.4.12</version>
<spring-cloud.version>2024.0.3</spring-cloud.version>
```

Motivo:
Mover Spring Cloud Gateway desde la linea `4.1.6` a una linea `4.2.x` corregida frente a `CVE-2025-41243`.

### 3. ms-auth

Archivo: `ms-auth/pom.xml`

Antes:
```xml
<spring-security.version>6.3.8</spring-security.version>
```

Despues:
```xml
<spring-security.version>6.5.9</spring-security.version>
```

Motivo:
Forzar una version corregida de `spring-security-web` para retirar `CVE-2026-22732`.

### 4. ms-config-server

Archivo: `ms-config-server/pom.xml`

Antes:
```xml
<spring-security.version>6.3.8</spring-security.version>
```

Despues:
```xml
<spring-security.version>6.5.9</spring-security.version>
```

Motivo:
Aplicar la misma correccion de Spring Security para retirar `CVE-2026-22732`.

## Resultado esperado

- `ms-registry-server` sin criticos en Snyk
- `ms-gateway-service` sin criticos en Snyk
- `ms-auth` sin criticos en Snyk
- `ms-config-server` sin criticos en Snyk

## Estado final

Los criticos reportados por Snyk para estos microservicios fueron corregidos y verificados posteriormente en Snyk.
![](https://www.plantuml.com/plantuml/png/RPC_J_Cm4CRt-nHMf_ST8XOc1jHIAlw8IeKK1iJYfaUmciPmTehoxR5pZbnR6Vjx_3jV7gjpOoNPxjW0M6KR5CjBKOZoMz91jVKxaXPhLP4sI2TLeG6GbTKaNaqdIMa0mihAjdRcRB5v47VIug_iFzf_HrFK_AC8yl-Pa4O4c9gT_H8bds9LEf9wdfmxoqPXQp7LAeQf6U2v_EQ0JDMJHwaue7FjHkj3QkqSIILFaezRTOG7lWo9BS9Q6KjPCpWSJBvHjv-gZcer_2xsqafM601uifF6AUk-V7uQicDlVXg0y5AYAAx7xiMLkDzkD-N5owhS0epKd_0jkzW72GyTJaJSqbJWcnWv-C6UybeEFsuMC4xDEO_CgP-NmpWurESl7Df5YkCdKxvaEkEhfg9OLnBVAbFf4r8V5mgFuq9-ncITF8lBP4bS9GzWZktU_tF_0000)
