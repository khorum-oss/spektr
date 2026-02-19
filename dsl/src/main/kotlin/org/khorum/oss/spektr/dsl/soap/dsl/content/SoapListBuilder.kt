package org.khorum.oss.spektr.dsl.soap.dsl.content

class SoapListBuilder(private val name: String) : SoapElementHolder(), SoapChild {
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