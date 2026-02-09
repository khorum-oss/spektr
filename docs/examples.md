# Example Applications

Spektr includes example applications demonstrating how to use it as a mock service for integration testing.

## Project Structure

```
examples/
├── common/                          # Shared models and JAXB classes
├── ghost-book/                      # SOAP service (Spring WS)
│   └── test-api/                    # Mock endpoints JAR for ghost-book
├── haunted-house-tracker/           # Main example application
│   └── test-api/                    # Mock endpoints JAR (GhostApi)
└── docker/                          # Docker Compose setup for testing
    └── endpoint-jars/               # JAR files loaded by Spektr
```

## Applications

### Haunted House Tracker

A sample application that manages haunted houses and their resident ghosts. It communicates with a "Ghost Book" SOAP service to manage ghost records.

**Features:**
- REST API for managing haunted houses
- SOAP client for ghost management
- Integration with Spektr for testing

**Running locally:**
```bash
./gradlew :examples:haunted-house-tracker:bootRun
```

**Running with Spektr mock:**
```bash
# Profile "test" connects to Spektr on port 8083
./gradlew :examples:haunted-house-tracker:bootRun --args='--spring.profiles.active=test'
```

### Ghost Book

A SOAP service that manages ghost records. Uses Spring WS with JAXB for XML marshalling.

**Features:**
- SOAP endpoints for CRUD operations on ghosts
- WSDL generation from JAXB-annotated classes
- XSD auto-generation

**Running locally:**
```bash
./gradlew :examples:ghost-book:bootRun
```

## Test API JARs

Each example application has a `test-api` submodule that produces a JAR file for Spektr to load.

### Building Test APIs

```bash
# Build individual test-api JAR (auto-copies to docker/endpoint-jars)
./gradlew :examples:haunted-house-tracker:test-api:jar

# Build all test-api JARs
./gradlew :examples:haunted-house-tracker:test-api:jar :examples:ghost-book:test-api:jar
```

### Version Management

Test API JARs have automatic version management:
- Version is stored in `test-api/version.txt`
- Each build increments the patch version
- Old JARs are automatically removed from `docker/endpoint-jars`

### GhostApi (haunted-house-tracker/test-api)

The `GhostApi` class implements `EndpointModule` to define mock SOAP endpoints:

```kotlin
class GhostApi : EndpointModule {
    override fun EndpointRegistry.configure() {
        // REST endpoints (none for this example)
    }

    override fun SoapEndpointRegistry.configureSoap() {
        // Create ghost
        operation("/ws", "CreateGhost") { request ->
            val type = extractElement(request.body, "type") ?: "Unknown"
            SoapResponse(body = """
                <ns:CreateGhostResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                  <ghost><type>$type</type></ghost>
                </ns:CreateGhostResponse>
            """.trimMargin())
        }

        // Get ghost by type
        operation("/ws", "GetGhost") { request ->
            // ...
        }

        // List all ghosts
        operation("/ws", "ListGhosts") { request ->
            // ...
        }
    }
}
```

**ServiceLoader Registration:**

The JAR includes `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule`:
```
org.khorum.oss.spektr.hauntedhousetracker.testapi.GhostApi
```

## Docker Setup

The `examples/docker` folder contains a Docker Compose setup for testing:

```yaml
services:
  spektr:
    build:
      context: ../..
      dockerfile: Dockerfile
    image: spektr:local
    ports:
      - "8083:8080"
    volumes:
      - ./endpoint-jars:/app/endpoint-jars
```

**Running:**
```bash
cd examples/docker
docker compose up -d
```

**Testing endpoints:**
```bash
# REST (if any)
curl http://localhost:8083/api/example

# SOAP
curl -X POST http://localhost:8083/ws \
  -H "Content-Type: text/xml" \
  -H "SOAPAction: GetGhost" \
  -d '<soap:Envelope>...</soap:Envelope>'
```

## Integration Testing

The example applications use Testcontainers to spin up Spektr as a mock service during tests.

See [Testing with Testcontainers](testing.md) for details.
