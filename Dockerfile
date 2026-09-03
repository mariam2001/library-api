# ---------- Stage 1: build the jar ----------
# Use a Maven image that already has JDK 21, so we don't rely on the ./mvnw wrapper
# (which can hit line-ending/permission issues inside Linux containers).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy just the pom first and pre-download dependencies. Docker caches each step as a
# "layer"; because the pom changes rarely, this expensive download is reused on later
# builds as long as pom.xml hasn't changed - only your source recompiles.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source and build. -DskipTests because our tests use Testcontainers, which
# needs its own Docker - not available inside this build container.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: the runtime image ----------
# A much smaller image with only the Java RUNTIME (no Maven, no JDK compiler, no source).
# Multi-stage keeps the final image lean - it ships just the jar + a JRE.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy ONLY the built jar out of the build stage above.
COPY --from=build /app/target/*.jar app.jar

# Document that the app listens on 8080 (informational; the actual publishing is done in
# docker-compose.yml).
EXPOSE 8080

# The command that runs when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
