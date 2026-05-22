# ─── Stage 1: BUILD ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper + pom.xml first (layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR — skip tests (tests run in CI)
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: RUNTIME ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Security: run as non-root user
RUN addgroup -S studysync && adduser -S studysync -G studysync

# Copy ONLY the JAR from build stage (~40MB vs ~600MB)
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown studysync:studysync app.jar

USER studysync

# Expose port (Render uses PORT env var)
EXPOSE 10000

# JVM flags for container environments
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]