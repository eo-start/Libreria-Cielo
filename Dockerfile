# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE
COPY ${MODULE}/target/*.jar app.jar
COPY docker/java-security.properties /app/java-security.properties
RUN mkdir -p /app/uploads && chown -R 10001:10001 /app
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+ExitOnOutOfMemoryError"
EXPOSE 7071 8081 8082 8083 8084 8090 8095
ENTRYPOINT ["java","-Djava.security.properties=/app/java-security.properties","-jar","/app/app.jar"]
