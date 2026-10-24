/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE
COPY artifacts/${MODULE}.jar /app/app.jar
COPY java-security.properties /app/java-security.properties
RUN mkdir -p /app/uploads && chown -R 10001:10001 /app
USER 10001:10001
ENTRYPOINT ["java","-Djava.security.properties=/app/java-security.properties","-jar","/app/app.jar"]
