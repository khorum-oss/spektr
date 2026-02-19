package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.SoapComponent
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml
import org.khorum.oss.spektr.dsl.soap.dsl.escapeXmlAttr

class SoapElementBuilder(
    private var name: String? = null,
    private val optional: Boolean = false,
    private val nillable: Boolean = false
) : SoapElementHolder(), SoapChild, SoapComponent {
    var content: Any? = null

    override fun toString(): String {
        val sb = StringBuilder()
        serialize(sb, false, "", 0)
        return sb.toString()
    }

    override fun toPrettyString(indent: String): String = buildString {
        serialize(this, pretty = true, indent, 0)
    }

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

        if (!hasContent()) {
            sb.append("/>")
            if (pretty) sb.appendLine()
            return
        }

        sb.append(">")
        when {
            hasCdata -> sb.appendCdata()
            hasRawXml -> sb.appendRawXml(pretty, indent, depth + 1)
            hasChildren -> sb.appendChildren(pretty, indent, depth + 1)
            else -> sb.appendText(content)
        }
        sb.append("</$n>")
        if (pretty) sb.appendLine()
    }

    private fun hasContent(): Boolean = content != null
        || cdata != null
        || rawXml != null
        || children.isNotEmpty()

    private fun StringBuilder.appendCdata() {
        append("<![CDATA[$cdata]]>")
    }

    private fun StringBuilder.appendRawXml(pretty: Boolean, indent: String, depth: Int) {
        if (pretty) appendLine()
        append(rawXml)
        if (pretty) appendLine()
        append(indent.repeat(depth))
    }

    private fun StringBuilder.appendChildren(pretty: Boolean, indent: String, depth: Int) {
        if (pretty) appendLine()
        serializeContent(this, pretty, indent, depth + 1)
        append(indent.repeat(depth))
    }

    private fun StringBuilder.appendText(text: Any?) { append(escapeXml(text.toString())) }
}