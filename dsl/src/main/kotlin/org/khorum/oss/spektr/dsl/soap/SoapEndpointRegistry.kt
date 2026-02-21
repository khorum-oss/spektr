package org.khorum.oss.spektr.dsl.soap

/**
 * Registry for defining and collecting SOAP endpoint operations.
 *
 * Provides a DSL for registering SOAP operations identified by path and SOAPAction.
 *
 * Example:
 * ```kotlin
 * val registry = soapEndpoints {
 *     operation("/ws/ghost", "getGhost") { request ->
 *         SoapResponse(body = "<soap:Envelope>...</soap:Envelope>")
 *     }
 *     operation("/ws/ghost", "createGhost") { request ->
 *         SoapResponse(status = 201, body = "...")
 *     }
 * }
 * ```
 */
class SoapEndpointRegistry {
    private val endpointList = mutableListOf<SoapEndpointDefinition>()

    /** Read-only list of all registered SOAP endpoint definitions. */
    val endpoints: List<SoapEndpointDefinition> get() = endpointList

    /**
     * Registers a SOAP operation.
     *
     * @param path The URL path where this operation is exposed.
     * @param soapAction The SOAPAction header value that identifies this operation.
     * @param handler The handler function to process requests.
     */
    fun operation(path: String, soapAction: String, handler: SoapHandler) {
        endpointList.add(SoapEndpointDefinition(path, soapAction, handler))
    }
}

/**
 * DSL entry point for creating a [SoapEndpointRegistry].
 *
 * @param block Configuration block for registering SOAP operations.
 * @return The configured registry containing all registered operations.
 */
fun soapEndpoints(block: SoapEndpointRegistry.() -> Unit): SoapEndpointRegistry {
    return SoapEndpointRegistry().apply(block)
}
