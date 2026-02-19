package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder

/**
 * Builder for the SOAP Header section.
 *
 * Extends [SoapElementHolder] to allow adding child elements, and implements
 * [SoapComponent] for serialization support.
 *
 * Example:
 * ```kotlin
 * header {
 *     element("ns:Authentication") {
 *         element("token") { content = "abc123" }
 *     }
 *     element("ns:Timestamp") { content = "2024-01-15T10:30:00Z" }
 * }
 * ```
 */
class SoapHeaderBuilder : SoapElementHolder(), SoapComponent {
    /** The envelope prefix to use for the Header element tag. */
    var prefix: String? = null

    /**
     * Returns compact XML without indentation.
     *
     * @return The serialized header as a single-line XML string.
     */
    override fun toString(): String {
        val sb = StringBuilder()
        serialize(sb, false, "", 0)
        return sb.toString()
    }

    /**
     * Returns formatted XML with indentation.
     *
     * @param indent The string to use for each indentation level.
     * @return The serialized header with pretty formatting.
     */
    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent, 0)
    }

    /**
     * Serializes the header to the given StringBuilder.
     *
     * @param sb The StringBuilder to append to.
     * @param pretty Whether to format with indentation.
     * @param indent The indentation string.
     * @param depth The current nesting depth.
     */
    internal fun serialize(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        sb.append(indent.repeat(depth))
        sb.append("<$prefix:Header>")
        if (pretty) sb.appendLine()
        serializeContent(sb, pretty, indent, depth + 1)
        sb.append(indent.repeat(depth))
        sb.append("</$prefix:Header>")
        if (pretty) sb.appendLine()
    }
}
