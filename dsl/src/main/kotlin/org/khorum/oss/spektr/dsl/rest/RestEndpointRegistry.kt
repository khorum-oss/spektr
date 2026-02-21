package org.khorum.oss.spektr.dsl.rest

/**
 * Registry for defining and collecting REST endpoint definitions.
 *
 * Provides a DSL for registering HTTP endpoints with various methods and includes
 * helper functions for creating common response types.
 *
 * Example:
 * ```kotlin
 * val registry = endpoints {
 *     get("/api/users") { request ->
 *         returnBody(listOf("user1", "user2"))
 *     }
 *     post("/api/users") { request ->
 *         returnResponse {
 *             status = 201
 *             header("Location", "/api/users/123")
 *             body = mapOf("id" to "123")
 *         }
 *     }
 * }
 * ```
 */
class RestEndpointRegistry {
    private val endpointList = mutableListOf<RestEndpointDefinition>()

    /** Read-only list of all registered endpoint definitions. */
    val endpoints: List<RestEndpointDefinition> get() = endpointList

    /**
     * Registers a GET endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun get(path: String, handler: DynamicHandler) = register(HttpMethod.GET, path, handler)

    /**
     * Registers a POST endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun post(path: String, handler: DynamicHandler) = register(HttpMethod.POST, path, handler)

    /**
     * Registers a PUT endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun put(path: String, handler: DynamicHandler) = register(HttpMethod.PUT, path, handler)

    /**
     * Registers a PATCH endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun patch(path: String, handler: DynamicHandler) = register(HttpMethod.PATCH, path, handler)

    /**
     * Registers a DELETE endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun delete(path: String, handler: DynamicHandler) = register(HttpMethod.DELETE, path, handler)

    /**
     * Registers an OPTIONS endpoint.
     *
     * @param path The URL path pattern for this endpoint.
     * @param handler The handler function to process requests.
     */
    fun options(path: String, handler: DynamicHandler) = register(HttpMethod.OPTIONS, path, handler)

    /**
     * Creates a response with only a body (status 200 OK).
     *
     * @param body The response body content.
     * @return A [DynamicResponse] with the given body.
     */
    fun returnBody(body: Any?): DynamicResponse = DynamicResponse(body = body)

    /**
     * Creates a response with only a status code (no body).
     *
     * @param status The HTTP status code.
     * @return A [DynamicResponse] with the given status.
     */
    fun returnStatus(status: Int): DynamicResponse = DynamicResponse(status = status)

    /**
     * Creates a response using the [DynamicResponse.Builder] DSL.
     *
     * @param scope Lambda with builder receiver for configuring the response.
     * @return The constructed [DynamicResponse].
     */
    fun returnResponse(scope: DynamicResponse.Builder.() -> Unit): DynamicResponse {
        return DynamicResponse.Builder().apply(scope).build()
    }

    private fun register(method: HttpMethod, path: String, handler: DynamicHandler) {
        endpointList.add(RestEndpointDefinition(method, path, handler))
    }

    /**
     * Registers an endpoint that returns an error response when a condition is met.
     *
     * Useful for simulating error scenarios in testing.
     *
     * @param method The HTTP method to respond to.
     * @param path The URL path pattern.
     * @param status The error status code to return.
     * @param body Optional error body content.
     * @param condition Function that determines whether to return the error (default: always).
     */
    fun errorOn(
        method: HttpMethod,
        path: String,
        status: Int,
        body: Any? = null,
        condition: (DynamicRequest) -> Boolean = { true }
    ) {
        register(method, path) { request ->
            if (condition(request)) {
                DynamicResponse(status = status, body = body)
            } else {
                DynamicResponse(status = 200, body = mapOf("status" to "ok"))
            }
        }
    }
}

/**
 * DSL entry point for creating a [RestEndpointRegistry].
 *
 * @param block Configuration block for registering endpoints.
 * @return The configured registry containing all registered endpoints.
 */
fun endpoints(block: RestEndpointRegistry.() -> Unit): RestEndpointRegistry {
    return RestEndpointRegistry().apply(block)
}
