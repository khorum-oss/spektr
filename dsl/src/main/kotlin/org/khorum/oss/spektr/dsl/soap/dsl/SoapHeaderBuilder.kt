package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder

class SoapHeaderBuilder : SoapElementHolder() {
    internal fun serialize(sb: StringBuilder, prefix: String, pretty: Boolean, indent: String, depth: Int) {
        sb.append(indent.repeat(depth))
        sb.append("<$prefix:Header>")
        if (pretty) sb.appendLine()
        serializeContent(sb, pretty, indent, depth + 1)
        sb.append(indent.repeat(depth))
        sb.append("</$prefix:Header>")
        if (pretty) sb.appendLine()
    }
}