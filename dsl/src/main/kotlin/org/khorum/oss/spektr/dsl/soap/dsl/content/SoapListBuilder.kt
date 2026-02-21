package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.SoapComponent

/**
 * Builder for list container elements that hold repeating child elements.
 *
 * Renders as a wrapper element containing zero or more child elements.
 * When empty, renders as a self-closing tag.
 *
 * Example:
 * ```kotlin
 * list("users") {
 *     element("user") { content = "Alice" }
 *     element("user") { content = "Bob" }
 *     element("user") { content = "Charlie" }
 * }
 * // Produces:
 * // <users>
 * //   <user>Alice</user>
 * //   <user>Bob</user>
 * //   <user>Charlie</user>
 * // </users>
 * ```
 *
 * @property name The list container element tag name.
 */
class SoapListBuilder(private val name: String) : SoapElementHolder(), SoapChild, SoapComponent {

    /**
     * Returns compact XML without indentation.
     *
     * @return The serialized list as a single-line XML string.
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
     * @return The serialized list with pretty formatting.
     */
    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent, 0)
    }

    /**
     * Serializes this list to the given StringBuilder.
     *
     * @param sb The StringBuilder to append to.
     * @param pretty Whether to format with indentation.
     * @param indent The indentation string.
     * @param depth The current nesting depth.
     */
    internal fun serialize(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        if (children.isEmpty()) {
            sb.append(indent.repeat(depth))
            sb.append("<$name/>")
            if (pretty) sb.appendLine()
            return
        }

        sb.append(indent.repeat(depth))
        sb.append("<$name>")
        if (pretty) sb.appendLine()
        serializeContent(sb, pretty, indent, depth + 1)
        sb.append(indent.repeat(depth))
        sb.append("</$name>")
        if (pretty) sb.appendLine()
    }
}
