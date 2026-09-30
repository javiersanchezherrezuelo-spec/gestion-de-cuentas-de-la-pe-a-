# Construye la web de React y el backend en una sola imagen.
# La usan Railway, Render, Fly.io o cualquier servidor con Docker.

# 1. Web (React)
FROM node:24-alpine AS web
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2. Backend (Spring Boot), con la web dentro
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY backend/ ./
COPY --from=web /app/frontend/dist ./src/main/resources/static
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# 3. Imagen final, solo con Java y el .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Europe/Madrid
COPY --from=backend /app/target/pena-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
