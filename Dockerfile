# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy module build files
COPY app/build.gradle.kts app/
COPY dsl/build.gradle.kts dsl/

# Create empty examples module (required by settings.gradle.kts)
RUN mkdir -p examples/src/main/kotlin && echo "" > examples/build.gradle.kts

# Download dependencies (cached layer)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code
COPY dsl/src dsl/src
COPY app/src app/src

# Build the application
RUN ./gradlew :app:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create directory for endpoint JARs
RUN mkdir -p /app/endpoint-jars

# Copy the built application
COPY --from=build /app/app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set default environment variables
ENV ENDPOINT_JARS_DIR=/app/endpoint-jars
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --endpoint-jars.dir=$ENDPOINT_JARS_DIR"]