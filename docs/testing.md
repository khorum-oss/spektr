# Testing with Testcontainers

Spektr can be used as a mock service in integration tests using Testcontainers. This provides a real HTTP/SOAP server for your tests without complex mocking.

## Prerequisites

Add test dependencies to your `build.gradle.kts`:

```kotlin
testImplementation(project(":examples:test-common"))  // Shared test utilities
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:testcontainers:2.0.2")
testImplementation("org.testcontainers:junit-jupiter:1.21.3")
testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
```

## Quick Start with @WithSpektr

The simplest way to use Spektr in tests is with the `@WithSpektr` annotation from `test-common`:

```kotlin
@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [MyApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WithSpektr(
    endpointJarsPath = "../docker/endpoint-jars",
    properties = ["external-service.base-url"]
)
class MyIntegrationTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    @Test
    fun `test with mock service`() {
        webTestClient.get()
            .uri("/my-endpoint")
            .exchange()
            .expectStatus().isOk
    }
}
```

### @WithSpektr Options

| Parameter | Default | Description |
|-----------|---------|-------------|
| `image` | `"spektr:local"` | Docker image to use |
| `endpointJarsPath` | `""` | Path to directory containing endpoint JARs |
| `restEnabled` | `true` | Enable REST endpoint loading |
| `soapEnabled` | `true` | Enable SOAP endpoint loading |
| `properties` | `[]` | Spring property names to set to Spektr's base URL |

## Setup

### 1. Build the Spektr Docker Image

Before running tests, build the `spektr:local` image:

```bash
docker build -t spektr:local .
```

### 2. Build Test API JARs

Build and deploy your mock endpoint JARs:

```bash
./gradlew :examples:haunted-house-tracker:test-api:shadowJar
./gradlew :examples:ghost-book:test-api:shadowJar
```

This automatically copies the JAR to `examples/docker/endpoint-jars`.

### 3. Gradle Task Integration

Add tasks to your module's `build.gradle.kts` to automate the setup:

```kotlin
// Build the Spektr Docker image (skip if already exists)
tasks.register<Exec>("buildSpektrImage") {
    group = "docker"
    description = "Builds the spektr:local Docker image"
    workingDir = rootProject.projectDir
    commandLine("docker", "build", "-t", "spektr:local", ".")

    onlyIf {
        val process = ProcessBuilder("docker", "images", "-q", "spektr:local")
            .redirectErrorStream(true)
            .start()
        process.inputStream.read().toString().isEmpty()
    }
}

// Prepare test environment
tasks.register("prepareTestEnv") {
    group = "verification"
    description = "Prepares test environment: builds Docker image and test-api JARs"
    dependsOn("buildSpektrImage")
    dependsOn(":examples:ghost-book:test-api:shadowJar")  // Your test-api
}

tasks.test {
    useJUnitPlatform()
    dependsOn("prepareTestEnv")
}
```

## TestClient DSL

The `test-common` module provides a `TestClient` DSL for cleaner REST assertions:

```kotlin
@WithSpektr(endpointJarsPath = "../docker/endpoint-jars", properties = ["my-service.base-url"])
class MyControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    private val testClient: TestClient by lazy {
        configureShared(webTestClient, "/api/items")
    }

    @Test
    fun `create item`() {
        testClient.post {
            withBody(CreateItemRequest(name = "Test"))
            expect {
                hasOkStatus()
                "$.name".jsonPathValueEquals("Test")
                "$.id".jsonPathValueExists()
            }
        }
    }

    @Test
    fun `list items`() {
        testClient.get {
            expect {
                hasOkStatus()
                "$.length()".jsonPath { isNotEmpty }
            }
        }
    }
}
```

## Complete Test Examples

### REST API Test (Haunted House Tracker)

```kotlin
@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [HauntedHouseTrackerApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WithSpektr(
    endpointJarsPath = "../docker/endpoint-jars",
    properties = ["ghost-book.base-url"]
)
class HauntedHouseControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    private val testClient by lazy { configureShared(webTestClient, "/haunted-houses") }

    @Test
    fun `create haunted house`() {
        val request = CreateHauntedHouseRequest(
            address = UsAddress(
                streetLine1 = "123 Elm Street",
                city = "Minneapolis",
                state = "Minnesota",
                postalCode = "55408"
            )
        )

        testClient.post {
            withBody(request)
            expect {
                hasOkStatus()
                "$.address.streetLine1".jsonPathValueEquals("123 Elm Street")
            }
        }
    }
}
```

### SOAP Endpoint Test (Ghost Book)

```kotlin
@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [GhostBookApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WithSpektr(
    endpointJarsPath = "../docker/endpoint-jars",
    properties = ["haunted-house-tracker.base-url"]
)
class GhostEndpointTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    private val namespace = "http://org.khorum-oss.com/ghost-book"

    private fun soapEnvelope(body: String) = """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:gh="$namespace">
    <soap:Body>
        $body
    </soap:Body>
</soap:Envelope>"""

    @Test
    fun `create ghost via SOAP`() {
        val request = soapEnvelope("""
            <gh:createGhostRequest>
                <type>Poltergeist</type>
                <origin>Germany</origin>
            </gh:createGhostRequest>
        """.trimIndent())

        webTestClient
            .post()
            .uri("/ws")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .xpath("//ghost/type").isEqualTo("Poltergeist")
            .xpath("//ghost/origin").isEqualTo("Germany")
    }
}
```

## Manual Container Configuration

For more control, configure the container manually:

```kotlin
@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [MyApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MyIntegrationTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    companion object {
        val spektrContainer: GenericContainer<*> = GenericContainer(
            DockerImageName.parse("spektr:local")
        )
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                MountableFile.forHostPath("../docker/endpoint-jars"),
                "/app/endpoint-jars"
            )
            .withEnv("SPEKTR_REST_ENABLED", "true")
            .withEnv("SPEKTR_SOAP_ENABLED", "true")
            .waitingFor(
                Wait.forHttp("/actuator/health")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(60))
            )
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("my-service.base-url") {
                "http://${spektrContainer.host}:${spektrContainer.firstMappedPort}"
            }
        }
    }

    @Test
    fun `test integration with mock service`() {
        webTestClient.post()
            .uri("/my-endpoint")
            .exchange()
            .expectStatus().isOk
    }
}
```

## Key Components

### Container Configuration (Manual Setup)

| Method | Description |
|--------|-------------|
| `withExposedPorts(8080)` | Expose Spektr's port |
| `withCopyFileToContainer(...)` | Copy endpoint JARs into container |
| `withEnv(...)` | Set environment variables |
| `waitingFor(...)` | Wait strategy for container readiness |
| `.apply { start() }` | Start container immediately |

### Dynamic Properties

Use `@DynamicPropertySource` to inject the container's URL into your application properties:

```kotlin
@DynamicPropertySource
@JvmStatic
fun configureProperties(registry: DynamicPropertyRegistry) {
    registry.add("external-service.url") {
        "http://${spektrContainer.host}:${spektrContainer.firstMappedPort}"
    }
}
```

### Wait Strategy

The health check wait strategy ensures Spektr is fully started:

```kotlin
.waitingFor(
    Wait.forHttp("/actuator/health")
        .forPort(8080)
        .withStartupTimeout(Duration.ofSeconds(60))
)
```

## Running Tests

```bash
# Run specific test class
./gradlew :examples:haunted-house-tracker:test --tests "*HauntedHouseControllerTest"
./gradlew :examples:ghost-book:test --tests "*GhostEndpointTest"

# Run all tests in a module
./gradlew :examples:haunted-house-tracker:test
./gradlew :examples:ghost-book:test
```

The `prepareTestEnv` task automatically:
1. Builds the `spektr:local` Docker image (if not already present)
2. Builds and deploys test-api JARs to `docker/endpoint-jars`

## CI/CD Integration

For GitHub Actions, ensure Docker is available and build the image before tests:

```yaml
- name: Build Spektr Docker image
  run: docker build -t spektr:local .

- name: Build test-api JARs
  run: |
    ./gradlew :examples:haunted-house-tracker:test-api:shadowJar
    ./gradlew :examples:ghost-book:test-api:shadowJar

- name: Run tests
  run: |
    ./gradlew :examples:haunted-house-tracker:test
    ./gradlew :examples:ghost-book:test
```

## Troubleshooting

### Container fails to start

1. Verify Docker is running: `docker ps`
2. Check the image exists: `docker images | grep spektr`
3. Increase startup timeout if needed

### Health check fails

1. Ensure endpoint JARs are in the correct location
2. Check container logs: `docker logs <container-id>`
3. Verify the JAR path in `withCopyFileToContainer` or `endpointJarsPath`

### Connection refused

1. Use `spektrContainer.host` not `localhost`
2. Use `spektrContainer.firstMappedPort` not the internal port (8080)
3. Ensure the `properties` in `@WithSpektr` match your application.yml property names

### SOAP XML Parsing Errors

When testing SOAP endpoints:
1. Ensure the XML declaration is at the very start of the string (no leading whitespace)
2. Use `MediaType.TEXT_XML` for the content type
3. Match the namespace exactly as defined in the JAXB classes

### Test API JAR Not Loading

1. Rebuild the JAR: `./gradlew :examples:ghost-book:test-api:shadowJar`
2. Check `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule` exists in the JAR
3. Verify the class name in the services file is correct
