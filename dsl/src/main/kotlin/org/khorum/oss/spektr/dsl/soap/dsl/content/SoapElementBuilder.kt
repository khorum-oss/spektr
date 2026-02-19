package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXmlAttr

class SoapElementBuilder(
    private var name: String? = null,
    private val optional: Boolean = false,
    private val nillable: Boolean = false
) : SoapElementHolder(), SoapChild {
    var content: Any? = null

    internal fun serialize(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        val n = name ?: return

        // Handle optional elements with no content
        if (optional && !hasContent()) return

        // Handle nillable elements with no content
        if (nillable && !hasContent()) {
            sb.append(indent.repeat(depth))
            sb.append("<$n xsi:nil=\"true\"/>")
            if (pretty) sb.appendLine()
            return
        }

        sb.append(indent.repeat(depth))
        sb.append("<$n")
        attributes.forEach { (k, v) -> sb.append(" $k=\"${escapeXmlAttr(v)}\"") }

        val hasChildren = children.isNotEmpty()
        val hasCdata = cdata != null
        val hasRawXml = rawXml != null
        val hasText = content != null

        if (!hasChildren && !hasCdata && !hasRawXml && !hasText) {
            sb.append("/>")
            if (pretty) sb.appendLine()
            return
        }

        sb.append(">")
        when {
            hasCdata -> sb.append("<![CDATA[$cdata]]>")
            hasRawXml -> {
                if (pretty) sb.appendLine()
                sb.append(rawXml)
                if (pretty) sb.appendLine()
                sb.append(indent.repeat(depth))
            }
            hasChildren -> {
                if (pretty) sb.appendLine()
                serializeContent(sb, pretty, indent, depth + 1)
                sb.append(indent.repeat(depth))
            }
            hasText -> sb.append(escapeXml(content.toString()))
        }
        sb.append("</$n>")
        if (pretty) sb.appendLine()
    }

    private fun hasContent(): Boolean = content != null || cdata != null || rawXml != null || children.isNotEmpty()
}