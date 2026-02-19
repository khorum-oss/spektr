package org.khorum.oss.spektr.dsl.soap

import org.khorum.oss.spektr.dsl.soap.dsl.SoapEnvelopeBuilder

/**
 * Serializes a [SoapEnvelopeBuilder] to an XML string.
 *
 * This class delegates to the builder's built-in serialization methods,
 * providing a consistent API for XML generation with configurable formatting.
 *
 * @property prettyPrint Whether to format the output with indentation and newlines (default: true).
 * @property indent The string to use for each indentation level (default: two spaces).
 */
class SoapXmlSerializer(
    private val prettyPrint: Boolean = true,
    private val indent: String = "  "
) {
    /**
     * Converts a [SoapEnvelopeBuilder] to an XML string.
     *
     * @param envelope The SOAP envelope builder to serialize.
     * @return The serialized XML string.
     */
    fun serialize(envelope: SoapEnvelopeBuilder): String {
        return if (prettyPrint) {
            envelope.toPrettyString(indent)
        } else {
            envelope.toString()
        }
    }
}

/**
 * Inline value class wrapping a SOAP XML string.
 *
 * Provides type safety to distinguish raw strings from SOAP XML content.
 *
 * @property content The underlying XML string.
 */
@JvmInline
value class SoapXml(val content: String)

/**
 * Extension function to convert a [SoapEnvelopeBuilder] to a [SoapXml] wrapper.
 *
 * @param prettyPrint Whether to format the output with indentation and newlines (default: true).
 * @param indent The string to use for each indentation level (default: two spaces).
 * @return The serialized XML wrapped in a [SoapXml] instance.
 */
fun SoapEnvelopeBuilder.toXml(prettyPrint: Boolean = true, indent: String = "  "): SoapXml {
    return if (prettyPrint) {
        SoapXml(toPrettyString(indent))
    } else {
        SoapXml(toString())
    }
}
