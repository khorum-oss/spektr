package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.SoapComponent

class SoapListBuilder(private val name: String) : SoapElementHolder(), SoapChild, SoapComponent {

    override fun toString(): String {
        val sb = StringBuilder()
        serialize(sb, false, "", 0)
        return sb.toString()
    }

    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent, 0)
    }

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