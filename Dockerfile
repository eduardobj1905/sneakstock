# Imagen única de SneakStock: el frontend compilado se sirve desde el propio Spring Boot,
# así la app y la API salen del mismo origen (sin proxy ni CORS en producción).

# 1. Compilar el frontend (Vue + Vite)
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2. Compilar el backend con el frontend dentro de sus recursos estáticos
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
COPY --from=frontend /app/frontend/dist src/main/resources/static
# Los tests se ejecutan en la integración continua (necesitan Docker para Testcontainers)
RUN ./mvnw -q -B package -DskipTests

# 3. Imagen final: solo el JRE y el .jar, con un usuario sin privilegios
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home sneakstock
COPY --from=backend /app/target/*.jar app.jar
USER sneakstock
# La JVM se ajusta a la memoria del contenedor (los planes gratuitos suelen dar 512 MB)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
