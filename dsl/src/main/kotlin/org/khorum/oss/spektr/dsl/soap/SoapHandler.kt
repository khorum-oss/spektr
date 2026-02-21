package org.khorum.oss.spektr.dsl.soap

/**
 * Functional interface for handling SOAP requests.
 *
 * Implementations of this interface process incoming [SoapRequest]s and produce
 * [SoapResponse]s. As a functional interface, handlers can be defined using lambda syntax.
 *
 * Example:
 * ```kotlin
 * val handler: SoapHandler = { request ->
 *     val responseXml = soapEnvelope {
 *         body {
 *             element("ns:Response") { content = "Success" }
 *         }
 *     }.toPrettyString()
 *     SoapResponse(body = responseXml)
 * }
 * ```
 */
fun interface SoapHandler {
    /**
     * Processes an incoming SOAP request and returns a response.
     *
     * @param request The incoming SOAP request with headers, SOAPAction, and XML body.
     * @return The SOAP response to send back to the client.
     */
    fun handle(request: SoapRequest): SoapResponse
}
