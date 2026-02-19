package org.khorum.oss.spektr.dsl.rest

/**
 * Functional interface for handling dynamic REST requests.
 *
 * Implementations of this interface process incoming [DynamicRequest]s and produce
 * [DynamicResponse]s. As a functional interface, handlers can be defined using lambda syntax.
 *
 * Example:
 * ```kotlin
 * val handler: DynamicHandler = { request ->
 *     DynamicResponse(body = mapOf("message" to "Hello, World!"))
 * }
 * ```
 */
fun interface DynamicHandler {
    /**
     * Processes an incoming request and returns a response.
     *
     * @param request The incoming HTTP request with headers, path variables, query params, and body.
     * @return The response to send back to the client.
     */
    fun handle(request: DynamicRequest): DynamicResponse
}
