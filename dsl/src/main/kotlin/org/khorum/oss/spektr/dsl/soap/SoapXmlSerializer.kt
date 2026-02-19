package org.khorum.oss.spektr.dsl.soap

import org.khorum.oss.spektr.dsl.soap.dsl.SoapEnvelopeBuilder

/**
 * Serializes a [org.khorum.oss.spektr.dsl.soap.dsl.SoapEnvelopeBuilder] to XML string.
 * Delegates to the builder's built-in serialization methods.
 */
class SoapXmlSerializer(
    private val prettyPrint: Boolean = true,
    private val indent: String = "  "
) {
    /**
     * Converts a [org.khorum.oss.spektr.dsl.soap.dsl.SoapEnvelopeBuilder] to an XML string.
     */
    fun serialize(envelope: SoapEnvelopeBuilder): String {
        return if (prettyPrint) {
            envelope.toPrettyString(indent)
        } else {
            envelope.toString()
        }
    }
}

@JvmInline
value class SoapXml(val content: String)

/**
 * Extension function to convert a [SoapEnvelopeBuilder] to XML.
 */
fun SoapEnvelopeBuilder.toXml(prettyPrint: Boolean = true, indent: String = "  "): SoapXml {
    return if (prettyPrint) {
        SoapXml(toPrettyString(indent))
    } else {
        SoapXml(toString())
    }
}
