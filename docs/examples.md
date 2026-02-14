# Example Applications

Spektr includes example applications demonstrating how to use it as a mock service for integration testing.

## Project Structure

```
examples/
├── common/                          # Shared domain models (JAXB + Jackson annotated)
├── test-common/                     # Shared test utilities (@WithSpektr, TestClient)
├── ghost-book/                      # SOAP service (Spring WS) with WebClient
│   └── test-api/                    # Mock for haunted-house-tracker REST API
├── haunted-house-tracker/           # REST service with SOAP client
│   └── test-api/                    # Mock for ghost-book SOAP API
└── docker/                          # Docker Compose setup for testing
    └── endpoint-jars/               # JAR files loaded by Spektr
```

## Application Architecture

The two example applications demonstrate bidirectional communication:

```
┌─────────────────────┐         SOAP          ┌─────────────────────┐
│   Haunted House     │ ───────────────────▶  │     Ghost Book      │
│      Tracker        │                       │                     │
│    (REST API)       │  ◀───────────────────  │   (SOAP Service)   │
│                     │         REST          │                     │
└─────────────────────┘                       └─────────────────────┘
       Port 8082                                    Port 8081
```

## Applications

### Haunted House Tracker

A REST application that manages haunted houses and their resident ghosts. It communicates with the Ghost Book SOAP service to manage ghost records.

**Features:**
- REST API for managing haunted houses (`/haunted-houses`)
- SOAP client (`GhostSoapClient`) for ghost management
- Integration with Spektr for testing

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/haunted-houses` | List all haunted houses |
| GET | `/haunted-houses/{id}` | Get house by ID |
| POST | `/haunted-houses` | Create a new haunted house |

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

A SOAP service that manages ghost records. Uses Spring WS with JAXB for XML marshalling and includes a WebClient to call the Haunted House Tracker REST API.

**Features:**
- SOAP endpoints for CRUD operations on ghosts (`/ws`)
- WebClient integration to call haunted-house-tracker
- WSDL generation from JAXB-annotated classes
- XSD auto-generation

**SOAP Operations:**
| Operation | Description |
|-----------|-------------|
| `createGhostRequest` | Create a ghost, optionally with associated haunted houses |
| `getGhostRequest` | Get a ghost by type |
| `listGhostsRequest` | List all ghosts |

**Running locally:**
```bash
./gradlew :examples:ghost-book:bootRun
```

**Configuration:**
```yaml
# application.yml
server:
  port: 8081

haunted-house-tracker:
  base-url: ${HAUNTED_HOUSE_TRACKER_BASE_URL:http://localhost:8082}
```

## Test API JARs

Each example application has a `test-api` submodule that produces a JAR file for Spektr to load. These mocks simulate the *other* service:

| Application | test-api Mocks | Purpose |
|-------------|----------------|---------|
| haunted-house-tracker | GhostApi (SOAP) | Mocks ghost-book's SOAP endpoints |
| ghost-book | HauntedHouseTrackerApi (REST) | Mocks haunted-house-tracker's REST API |

### Building Test APIs

```bash
# Build individual test-api JAR (auto-copies to docker/endpoint-jars)
./gradlew :examples:haunted-house-tracker:test-api:shadowJar
./gradlew :examples:ghost-book:test-api:shadowJar

# Build all test-api JARs
./gradlew :examples:haunted-house-tracker:test-api:shadowJar :examples:ghost-book:test-api:shadowJar
```

### Version Management

Test API JARs have automatic version management:
- Version is stored in `test-api/version.txt`
- Each build increments the patch version
- Old JARs are automatically removed from `docker/endpoint-jars`

### GhostApi (haunted-house-tracker/test-api)

The `GhostApi` class mocks the Ghost Book SOAP service for haunted-house-tracker tests:

```kotlin
class GhostApi : EndpointModule {
    override fun RestEndpointRegistry.configure() {
        // No REST endpoints
    }

    override fun SoapEndpointRegistry.configureSoap() {
        operation("/ws", "CreateGhost") { request ->
            val type = extractElement(request.body, "type") ?: "Unknown"
            SoapResponse(body = """
                <ns:createGhostResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                  <ghost><type>$type</type></ghost>
                </ns:createGhostResponse>
            """.trimIndent())
        }

        operation("/ws", "ListGhosts") { _ ->
            SoapResponse(body = """
                <ns:listGhostsResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                  <ghost><type>Poltergeist</type></ghost>
                  <ghost><type>Specter</type></ghost>
                </ns:listGhostsResponse>
            """.trimIndent())
        }
    }
}
```

### HauntedHouseTrackerApi (ghost-book/test-api)

The `HauntedHouseTrackerApi` class mocks the Haunted House Tracker REST API for ghost-book tests:

```kotlin
class HauntedHouseTrackerApi : EndpointModule {
    private val store = mutableMapOf<UUID, HauntedHouse>(/* pre-populated data */)

    override fun RestEndpointRegistry.configure() {
        get("/haunted-houses") {
            returnBody(store.values.toList())
        }

        get("/haunted-houses/{id}") { request ->
            val id = request.pathVariables["id"]?.let { UUID.fromString(it) }
            val house = id?.let(store::get)
            returnResponse {
                options {
                    notFound(house == null, "House not found")
                    ok(house)
                }
            }
        }

        post("/haunted-houses") { request ->
            val body = OBJECT_MAPPER.readValue(request.body, CreateHauntedHouseRequest::class.java)
            // Creates house only for specific test address
            val house = if (body?.address?.streetLine1 == "1677 Round Top Rd") {
                HauntedHouse(id = UUID.randomUUID(), address = body.address!!)
            } else null

            returnResponse {
                options {
                    badRequest(house == null, "Failed to create house")
                    ok(house)
                }
            }
        }
    }
}
```

**ServiceLoader Registration:**

Each JAR includes `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule`:
```
# haunted-house-tracker/test-api
org.khorum.oss.spektr.hauntedhousetracker.testapi.GhostApi

# ghost-book/test-api
org.khorum.oss.spektr.ghostbook.testapi.HauntedHouseTrackerApi
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

### Running Tests

```bash
# Run haunted-house-tracker tests (uses ghost-book mock)
./gradlew :examples:haunted-house-tracker:test

# Run ghost-book tests (uses haunted-house-tracker mock)
./gradlew :examples:ghost-book:test
```

### Test Setup

Both applications use the `@WithSpektr` annotation to simplify test configuration:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithSpektr(
    endpointJarsPath = "../docker/endpoint-jars",
    properties = ["ghost-book.base-url"]  // Property to set to Spektr's URL
)
class HauntedHouseControllerTest {
    // tests...
}
```

The test framework:
1. Starts a Spektr Docker container
2. Loads endpoint JARs from `docker/endpoint-jars`
3. Sets the specified Spring properties to point to Spektr's URL
4. Runs tests against your application which calls the mocked services

See [Testing with Testcontainers](testing.md) for complete documentation.

## Common Module

The `examples/common` module contains shared domain classes used by both applications:

| Class | Description |
|-------|-------------|
| `Ghost` | Ghost entity with JAXB + Jackson annotations |
| `HauntedHouse` | Haunted house entity with polymorphic Address |
| `Address` | Sealed interface with UsAddress, CaAddress, GenericAddress |
| `CreateGhostRequest/Response` | SOAP request/response types |
| `CreateHauntedHouseRequest` | REST request type |

### JAXB + Jackson Compatibility

Domain classes are annotated for both JAXB (SOAP/XML) and Jackson (REST/JSON):

```kotlin
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UsAddressType", propOrder = ["streetLine1", "streetLine2", ...])
data class UsAddress(
    @field:XmlElement(required = true)
    override val streetLine1: String = "",
    // ... other fields with defaults for JAXB no-arg constructor
) : Address

// Polymorphic type handling for JSON
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "country")
@JsonSubTypes(
    JsonSubTypes.Type(value = UsAddress::class, name = "US"),
    JsonSubTypes.Type(value = CaAddress::class, name = "CA")
)
sealed interface Address { ... }
```

## Test Common Module

The `examples/test-common` module provides shared test utilities:

| Class | Description |
|-------|-------------|
| `@WithSpektr` | JUnit extension annotation for Spektr container |
| `SpektrExtension` | JUnit extension that manages container lifecycle |
| `SpektrContainer` | Testcontainers wrapper for Spektr |
| `TestClient` | DSL for REST API testing with assertions |

### TestClient DSL

```kotlin
val testClient = configureShared(webTestClient, "/api/items")

testClient.post {
    withBody(request)
    expect {
        hasOkStatus()
        "$.id".jsonPathValueExists()
        "$.name".jsonPathValueEquals("Test")
    }
}

testClient.get("/123") {
    expect {
        hasOkStatus()
        "$.items".jsonPath { isNotEmpty }
    }
}
```
