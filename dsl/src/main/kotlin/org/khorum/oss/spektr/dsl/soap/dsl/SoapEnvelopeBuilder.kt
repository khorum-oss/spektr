package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapBodyContent
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapFaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

class SoapEnvelopeBuilder : SoapComponent {
    var version: SoapVersion = SoapVersion.V1_2
    var envelopePrefix: String = "soapenv"
    var schemasLocation: String? = null
    private var namespaces: SoapNamespacesBuilder? = null
    private var header: SoapHeaderBuilder? = null
    private var body: SoapBodyContent? = null

    @SoapDslMarker
    fun namespaces(block: SoapNamespacesBuilder.() -> Unit) {
        namespaces = SoapNamespacesBuilder().apply(block)
    }

    @SoapDslMarker
    fun header(block: SoapHeaderBuilder.() -> Unit) {
        header = SoapHeaderBuilder().apply(block)
    }

    @SoapDslMarker
    fun body(block: SoapBodyBuilder.() -> Unit) {
        checkBodyNotSet()
        body = SoapBodyBuilder(version).apply(block)
    }

    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        checkBodyNotSet()
        body = version.faultBuilder().apply(block)
    }

    private fun checkBodyNotSet() {
        if (body != null) throw IllegalStateException("Body already set")
    }

    /** Returns compact XML without indentation or newlines. */
    override fun toString(): String = buildString {
        serialize(this, pretty = false, indent = "", depth = 0)
    }

    /** Returns formatted XML with indentation for readability. */
    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent = indent, depth = 0)
    }


    private fun serialize(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        val soapNs = schemasLocation ?: SOAP_NAMESPACES[version]
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
