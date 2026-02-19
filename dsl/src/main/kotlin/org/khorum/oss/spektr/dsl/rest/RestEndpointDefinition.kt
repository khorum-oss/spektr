package org.khorum.oss.spektr.dsl.rest

/**
 * Defines a single REST endpoint with its HTTP method, path, and handler.
 *
 * This data class represents the complete definition of a REST endpoint that can be
 * registered and routed by the application's HTTP server.
 *
 * @property method The HTTP method this endpoint responds to (GET, POST, PUT, etc.).
 * @property path The URL path pattern, which may include path variables (e.g., `/users/{id}`).
 * @property handler The function that processes requests to this endpoint.
 */
data class RestEndpointDefinition(
    val method: HttpMethod,
    val path: String,
    val handler: DynamicHandler
)
