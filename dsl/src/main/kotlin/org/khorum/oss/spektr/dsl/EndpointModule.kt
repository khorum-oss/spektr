package org.khorum.oss.spektr.dsl

import org.khorum.oss.spektr.dsl.rest.RestEndpointRegistry
import org.khorum.oss.spektr.dsl.soap.SoapEndpointRegistry

/**
 * Interface for defining endpoint modules that configure REST and SOAP endpoints.
 *
 * Implement this interface to create modular endpoint configurations that can be
 * registered with the application's endpoint registries.
 *
 * Example:
 * ```kotlin
 * class MyEndpointModule : EndpointModule {
 *     override fun RestEndpointRegistry.configure() {
 *         get("/api/users") { request -> returnBody(listOf("user1", "user2")) }
 *     }
 *
 *     override fun SoapEndpointRegistry.configureSoap() {
 *         operation("/ws", "getUser") { request -> SoapResponse(body = "<user>...</user>") }
 *     }
 * }
 * ```
 */
interface EndpointModule {
    /**
     * Configures REST endpoints within the given registry.
     *
     * Override this method to define REST API endpoints using the registry's DSL methods.
     *
     * @receiver The REST endpoint registry to configure.
     */
    fun RestEndpointRegistry.configure() {
        // Default no-op implementation
    }

    /**
     * Configures SOAP endpoints within the given registry.
     *
     * Override this method to define SOAP operations using the registry's DSL methods.
     * The default implementation is a no-op, allowing modules to opt-in to SOAP support.
     *
     * @receiver The SOAP endpoint registry to configure.
     */
    fun SoapEndpointRegistry.configureSoap() {
        // Default no-op so existing modules don't need to implement SOAP
    }
}
