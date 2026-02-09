# Testing with Testcontainers

Spektr can be used as a mock service in integration tests using Testcontainers. This provides a real HTTP/SOAP server for your tests without complex mocking.

## Prerequisites

Add Testcontainers dependencies to your `build.gradle.kts`:

```kotlin
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:testcontainers:2.0.2")
testImplementation("org.testcontainers:junit-jupiter:1.20.6")
```

## Setup

### 1. Build the Spektr Docker Image

Before running tests, build the `spektr:local` image:

```bash
docker build -t spektr:local .
```

### 2. Build Test API JARs

Build and deploy your mock endpoint JARs:

```bash
./gradlew :examples:haunted-house-tracker:test-api:jar
```

This automatically copies the JAR to `examples/docker/endpoint-jars`.

### 3. Gradle Task Integration

Add tasks to your module's `build.gradle.kts` to automate the setup:

```kotlin
// Build the Spektr Docker image
tasks.register<Exec>("buildSpektrImage") {
    group = "docker"
    description = "Builds the spektr:local Docker image"
    workingDir = rootProject.projectDir
    commandLine("docker", "build", "-t", "spektr:local", ".")
}

// Prepare test environment
tasks.register("prepareTestEnv") {
    group = "verification"
    description = "Prepares test environment: builds Docker image and test-api JARs"
    dependsOn("buildSpektrImage")
    dependsOn(":examples:haunted-house-tracker:test-api:jar")
}

tasks.test {
    useJUnitPlatform()
    dependsOn("prepareTestEnv")
}
```

## Test Example

Here's a complete test class using Spektr as a mock SOAP server:

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
        // Your test using the mock service
        webTestClient.post()
            .uri("/my-endpoint")
            .exchange()
            .expectStatus().isOk
    }
}
```

## Key Components

### Container Configuration

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

# Run all tests in module
./gradlew :examples:haunted-house-tracker:test
```

The `prepareTestEnv` task automatically:
1. Builds the `spektr:local` Docker image
2. Builds and deploys test-api JARs

## CI/CD Integration

For GitHub Actions, ensure Docker is available and build the image before tests:

```yaml
- name: Build Spektr Docker image
  run: docker build -t spektr:local .

- name: Build test-api JARs
  run: ./gradlew :examples:haunted-house-tracker:test-api:jar

- name: Run tests
  run: ./gradlew :examples:haunted-house-tracker:test
```

## Troubleshooting

### Container fails to start

1. Verify Docker is running: `docker ps`
2. Check the image exists: `docker images | grep spektr`
3. Increase startup timeout if needed

### Health check fails

1. Ensure endpoint JARs are in the correct location
2. Check container logs: `docker logs <container-id>`
3. Verify the JAR path in `withCopyFileToContainer`

### Connection refused

1. Use `spektrContainer.host` not `localhost`
2. Use `spektrContainer.firstMappedPort` not the internal port (8080)
