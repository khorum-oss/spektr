# Spektr

A dynamic endpoint server that loads HTTP endpoints from external JAR files at runtime. 
Perfect for creating mock servers and test fixtures.

## Features

- **Dynamic endpoint loading** - Load endpoints from JAR files without restarting
- **Hot reload** - Add or update endpoint JARs and reload via API
- **DSL-based configuration** - Define endpoints using a simple Kotlin DSL
- **ServiceLoader discovery** - Automatically discovers `EndpointModule` implementations

## Quick Start

### 1. Build the application

```shell
./gradlew :app:bootJar
```

### 2. Create an endpoint JAR

Implement the `EndpointModule` interface:

```kotlin
package com.example.endpoints

import org.khorum.oss.spektr.dsl.*

class MyEndpoints : EndpointModule {
    override fun EndpointRegistry.configure() {
        get("/api/hello/{name}") { request ->
            val name = request.pathVariables["name"]
            DynamicResponse(body = mapOf("message" to "Hello, $name!"))
        }

        post("/api/users") { request ->
            DynamicResponse(status = 201, body = mapOf("created" to true))
        }
    }
}
```

Register it in `META-INF/services/org.khorum.oss.spektr.dsl.EndpointModule`:
```
com.example.endpoints.MyEndpoints
```

### 3. Run the server

```shell
java -jar app/build/libs/app.jar --endpoint-jars.dir=./my-jars
```

## Configuration

### Core Properties

| Property | Default | Description |
|----------|---------|-------------|
| `endpoint-jars.dir` | `./endpoint-jars` | Directory containing endpoint JAR files |

### Environment Variables

| Variable | Description |
|----------|-------------|
| `ENDPOINT_JARS_DIR` | Override the endpoint JARs directory |
| `JAVA_OPTS` | JVM options (when using Docker) |

### Custom Configuration

You can inject additional configuration using Spring Boot's standard mechanisms:

**Environment variable:**
```shell
SPRING_CONFIG_IMPORT=optional:file:./my-config.yaml
```

**Command line:**
```shell
java -jar app.jar --spring.config.import=optional:file:./my-config.yaml
```

**Multiple config files:**
```shell
java -jar app.jar --spring.config.additional-location=file:./custom.yaml
```

## Docker

### Build the image

```shell
docker build -t spektr .
```

### Run with default settings

```shell
docker run -p 8080:8080 spektr
```

### Run with endpoint JARs mounted

```shell
docker run -p 8080:8080 \
  -v /path/to/your/jars:/app/endpoint-jars \
  spektr
```

### Run with custom configuration

```shell
docker run -p 8080:8080 \
  -e SPRING_CONFIG_IMPORT=optional:file:/app/config/custom.yaml \
  -v /my/config:/app/config \
  -v /my/jars:/app/endpoint-jars \
  spektr
```

### Run with custom JVM options

```shell
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  spektr
```

## Admin API

### Reload endpoints

Reload all endpoints from the configured JAR directory:

```shell
curl -X POST http://localhost:8080/admin/endpoints/reload
```

Response:
```json
{
  "endpointsLoaded": 5,
  "jarsProcessed": 2,
  "reloadTimeMs": 42
}
```

### Upload and reload

Upload a new JAR file and reload endpoints:

```shell
curl -X POST http://localhost:8080/admin/endpoints/upload \
  -F "jar=@my-endpoints.jar"
```

## DSL Reference

### HTTP Methods

```kotlin
get("/path") { request -> DynamicResponse(...) }
post("/path") { request -> DynamicResponse(...) }
put("/path") { request -> DynamicResponse(...) }
patch("/path") { request -> DynamicResponse(...) }
delete("/path") { request -> DynamicResponse(...) }
options("/path") { request -> DynamicResponse(...) }
```

### Path Variables

```kotlin
get("/users/{id}") { request ->
    val id = request.pathVariables["id"]
    DynamicResponse(body = mapOf("id" to id))
}
```

### Request Properties

```kotlin
request.pathVariables   // Map<String, String> - path parameters
request.queryParams     // Map<String, List<String>> - query string
request.headers         // Map<String, List<String>> - HTTP headers
request.body            // String? - request body
```

### Response Options

```kotlin
DynamicResponse(
    status = 200,                           // HTTP status code
    body = mapOf("key" to "value"),         // Response body (auto-serialized to JSON)
    headers = mapOf("X-Custom" to "value")  // Response headers
)
```

### Error Scenarios

```kotlin
errorOn(
    method = HttpMethod.GET,
    path = "/api/error",
    status = 500,
    body = mapOf("error" to "Something went wrong")
)
```

## Development

### Run tests

```shell
./gradlew test
```

### Run locally with test profile

```shell
./gradlew :app:bootRun --args='--spring.profiles.active=test'
```

## License

MIT
