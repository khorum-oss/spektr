# Docker Setup

This guide covers running Spektr with Docker for development and testing.

## Building the Image

From the project root:

```bash
docker build -t spektr:local .
```

## Running Spektr

### Basic Run

```bash
docker run -p 8080:8080 spektr:local
```

### With Endpoint JARs

Mount a directory containing your endpoint JAR files:

```bash
docker run -p 8080:8080 \
  -v /path/to/your/jars:/app/endpoint-jars \
  spektr:local
```

### With Custom Configuration

```bash
docker run -p 8080:8080 \
  -e SPEKTR_REST_ENABLED=true \
  -e SPEKTR_SOAP_ENABLED=true \
  -v /path/to/jars:/app/endpoint-jars \
  spektr:local
```

## Docker Compose

The `examples/docker` folder contains a ready-to-use Docker Compose setup.

### docker-compose.yml

```yaml
services:
  spektr:
    build:
      context: ../..
      dockerfile: Dockerfile
    image: spektr:local
    container_name: spektr-app
    ports:
      - "8083:8080"
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
      - ENDPOINT_JARS_DIR=/app/endpoint-jars
      - SPEKTR_REST_ENABLED=true
      - SPEKTR_SOAP_ENABLED=true
    volumes:
      - ./endpoint-jars:/app/endpoint-jars
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 30s
```

### Running

```bash
cd examples/docker

# Start (builds image if needed)
docker compose up -d

# View logs
docker logs -f spektr-app

# Stop
docker compose down
```

### Rebuilding

After changing endpoint JARs or the Spektr source:

```bash
# Rebuild test-api JARs (auto-copies to endpoint-jars folder)
./gradlew :examples:haunted-house-tracker:test-api:jar

# Restart container to pick up changes
docker compose restart
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ENDPOINT_JARS_DIR` | `/app/endpoint-jars` | Directory containing endpoint JAR files |
| `SPEKTR_REST_ENABLED` | `true` | Enable REST endpoint loading |
| `SPEKTR_SOAP_ENABLED` | `true` | Enable SOAP endpoint loading |
| `JAVA_OPTS` | (empty) | JVM options |

## Health Check

Spektr exposes Spring Boot Actuator health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{"status": "UP"}
```

## Endpoint Verification

On startup, Spektr logs the loaded endpoints:

```
JarEndpointLoader : Loading endpoints from JARs in /app/endpoint-jars
JarEndpointLoader : Found EndpointModule: org.example.MyEndpoints
JarEndpointLoader : Loaded 3 REST endpoints from 1 JARs
JarEndpointLoader : Loaded 2 SOAP endpoints from 1 JARs
JarEndpointLoader : === REST Endpoints ===
JarEndpointLoader :   GET /api/users
JarEndpointLoader :   POST /api/users
JarEndpointLoader :   DELETE /api/users/{id}
JarEndpointLoader : === SOAP Endpoints ===
JarEndpointLoader :   POST /ws (SOAPAction: CreateGhost)
JarEndpointLoader :   POST /ws (SOAPAction: GetGhost)
```

## Reloading Endpoints

Reload endpoints without restarting the container:

```bash
curl -X POST http://localhost:8080/admin/endpoints/reload
```

Response:
```json
{
  "endpointsLoaded": 3,
  "soapEndpointsLoaded": 2,
  "jarsProcessed": 1,
  "reloadTimeMs": 45
}
```

## Troubleshooting

### No endpoints loaded

1. Check JAR files are in the mounted directory
2. Verify JARs contain `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule`
3. Check container logs for errors: `docker logs spektr-app`

### Container won't start

1. Check port 8080/8083 isn't already in use
2. Verify Docker is running
3. Check Docker logs for startup errors

### Changes not reflected

1. Rebuild the JAR: `./gradlew :examples:haunted-house-tracker:test-api:jar`
2. Restart the container: `docker compose restart`
3. Or call reload API: `curl -X POST http://localhost:8083/admin/endpoints/reload`
