package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder

class SoapHeaderBuilder : SoapElementHolder(), SoapComponent {
    var prefix: String? = null

    override fun toString(): String {
        val sb = StringBuilder()
        serialize(sb, false, "", 0)
        return sb.toString()
    }

    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent, 0)
    }

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