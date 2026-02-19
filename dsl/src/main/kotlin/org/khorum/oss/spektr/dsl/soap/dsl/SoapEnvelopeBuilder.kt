package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyContent
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapFaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

/**
 * Builder for constructing SOAP envelopes.
 *
 * This is the root builder class for the SOAP DSL, providing methods to configure
 * the envelope's version, namespaces, header, and body sections.
 *
 * Example:
 * ```kotlin
 * val envelope = SoapEnvelopeBuilder().apply {
 *     version = SoapVersion.V1_2
 *     namespaces { ns("xmlns:ns" to "http://example.com") }
 *     body { element("ns:Response") { content = "OK" } }
 * }
 * println(envelope.toPrettyString())
 * ```
 */
class SoapEnvelopeBuilder : SoapComponent {
    /** The SOAP version to use (default: SOAP 1.2). */
    var version: SoapVersion = SoapVersion.V1_2

    /** The XML prefix for SOAP envelope elements (default: "soapenv"). */
    var envelopePrefix: String = "soapenv"

    /** Optional custom schema location to override the default namespace URI. */
    var schemasLocation: String? = null

    private var namespaces: SoapNamespacesBuilder? = null
    private var header: SoapHeaderBuilder? = null
    private var body: SoapBodyContent? = null

    /**
     * Configures custom XML namespaces for the envelope.
     *
     * @param block Configuration block for defining namespace prefixes and URIs.
     */
    @SoapDslMarker
    fun namespaces(block: SoapNamespacesBuilder.() -> Unit) {
        namespaces = SoapNamespacesBuilder().apply(block)
    }

    /**
     * Configures the SOAP header section.
     *
     * @param block Configuration block for adding header elements.
     */
    @SoapDslMarker
    fun header(block: SoapHeaderBuilder.() -> Unit) {
        header = SoapHeaderBuilder().apply(block)
    }

    /**
     * Configures the SOAP body section with elements.
     *
     * Cannot be called if [fault] has already been called on this envelope.
     *
     * @param block Configuration block for adding body elements.
     * @throws IllegalStateException if body has already been set.
     */
    @SoapDslMarker
    fun body(block: SoapBodyBuilder.() -> Unit) {
        checkBodyNotSet()
        body = SoapBodyBuilder(version).apply(block)
    }

    /**
     * Configures the SOAP body as a fault response.
     *
     * Cannot be called if [body] has already been called on this envelope.
     * The fault structure depends on the configured [version].
     *
     * @param block Configuration block for defining the fault details.
     * @throws IllegalStateException if body has already been set.
     */
    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        checkBodyNotSet()
        body = version.faultBuilder().apply(block)
    }

    private fun checkBodyNotSet() {
        if (body != null) throw IllegalStateException("Body already set")
    }

    /**
     * Returns compact XML without indentation or newlines.
     *
     * @return The serialized SOAP envelope as a single-line XML string.
     */
    override fun toString(): String = buildString {
        serialize(this, pretty = false, indent = "", depth = 0)
    }

    /**
     * Returns formatted XML with indentation for readability.
     *
     * @param indent The string to use for each indentation level.
     * @return The serialized SOAP envelope with pretty formatting.
     */
    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent = indent, depth = 0)
    }

    private fun serialize(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        val soapNs = schemasLocation
            ?: SOAP_NAMESPACES[version]
            ?: throw IllegalArgumentException("Unknown SOAP version: $version")

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        if (pretty) sb.appendLine()

        sb.append(indent.repeat(depth))
        sb.append("<$envelopePrefix:Envelope xmlns:$envelopePrefix=\"$soapNs\"")
        namespaces?.getNamespaces()?.forEach { (attr, uri) -> sb.append(" $attr=\"$uri\"") }
        sb.append(">")
        if (pretty) sb.appendLine()

        header?.prefix = envelopePrefix
        header?.serialize(sb, pretty, indent, depth + 1)
        serializeBody(sb, pretty, indent, depth + 1)

        sb.append(indent.repeat(depth))
        sb.append("</$envelopePrefix:Envelope>")
        if (pretty) sb.appendLine()
    }

    private fun serializeBody(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        sb.append(indent.repeat(depth))
        sb.append("<$envelopePrefix:Body>")
        if (pretty) sb.appendLine()

        when (val b = body) {
            is SoapBodyBuilder -> {
                b.serializeContent(sb, pretty, indent, depth + 1)
                b.getFault()?.serialize(sb, envelopePrefix, pretty, indent, depth + 1)
            }
            is SoapFaultBuilder -> b.serialize(sb, envelopePrefix, pretty, indent, depth + 1)
            null -> {}
        }

        sb.append(indent.repeat(depth))
        sb.append("</$envelopePrefix:Body>")
        if (pretty) sb.appendLine()
    }

    companion object {
        private val SOAP_NAMESPACES = mapOf(
            SoapVersion.V1_1 to "http://schemas.xmlsoap.org/soap/envelope/",
            SoapVersion.V1_2 to "http://www.w3.org/2003/05/soap-envelope"
        )
    }
}
