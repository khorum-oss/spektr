package org.khorum.oss.spektr.dsl.soap.dsl

/**
 * Creates a SOAP envelope using the builder DSL.
 *
 * This is the main entry point for constructing SOAP messages using a type-safe DSL.
 *
 * Example:
 * ```kotlin
 * val envelope = soapEnvelope {
 *     version = SoapVersion.V1_2
 *     envelopePrefix = "env"
 *
 *     namespaces {
 *         ns("xmlns:ns" to "http://example.com/api")
 *     }
 *
 *     header {
 *         element("ns:token") { content = "abc123" }
 *     }
 *
 *     body {
 *         element("ns:GetUserResponse") {
 *             element("user") {
 *                 element("name") { content = "John Doe" }
 *                 element("email") { content = "john@example.com" }
 *             }
 *         }
 *     }
 * }
 *
 * println(envelope.toPrettyString())
 * ```
 *
 * @param block Configuration block for building the SOAP envelope.
 * @return The configured [SoapEnvelopeBuilder] ready for serialization.
 */
@SoapDslMarker
fun soapEnvelope(block: SoapEnvelopeBuilder.() -> Unit): SoapEnvelopeBuilder {
    return SoapEnvelopeBuilder().apply(block)
}
