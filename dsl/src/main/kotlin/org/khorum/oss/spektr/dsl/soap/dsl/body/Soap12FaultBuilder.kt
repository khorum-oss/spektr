package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultCode
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultReason

class Soap12FaultBuilder : SoapFaultBuilder() {
    private var code: SoapFaultCode? = null
    private var reason: SoapFaultReason? = null
    private var node: String? = null
    private var role: String? = null

    override fun code(value: String) {
        code = SoapFaultCode().apply { value(value) }
    }
    override fun code(block: SoapFaultCode.() -> Unit) { code = SoapFaultCode().apply(block) }
    override fun reason(block: SoapFaultReason.() -> Unit) { reason = SoapFaultReason().apply(block) }
    override fun node(node: String) { this.node = node }
    override fun role(role: String) { this.role = role }

    override fun serializeFaultContent(sb: StringBuilder, prefix: String, pretty: Boolean, indent: String, depth: Int) {
        code?.let { c ->
            sb.append(indent.repeat(depth))
            sb.append("<$prefix:Code>")
            if (pretty) sb.appendLine()
            c.getValue()?.let { v ->
                sb.append(indent.repeat(depth + 1))
                sb.append("<$prefix:Value>${escapeXml(v)}</$prefix:Value>")
                if (pretty) sb.appendLine()
            }
            serializeSubcodes(sb, c.getSubcodes(), prefix, pretty, indent, depth + 1)
            sb.append(indent.repeat(depth))
            sb.append("</$prefix:Code>")
            if (pretty) sb.appendLine()
        }
        reason?.let { r ->
            sb.append(indent.repeat(depth))
            sb.append("<$prefix:Reason>")
            if (pretty) sb.appendLine()
            sb.append(indent.repeat(depth + 1))
            sb.append("<$prefix:Text xml:lang=\"${r.lang ?: "en"}\">${escapeXml(r.text ?: "")}</$prefix:Text>")
            if (pretty) sb.appendLine()
            sb.append(indent.repeat(depth))
            sb.append("</$prefix:Reason>")
            if (pretty) sb.appendLine()
        }
        node?.let {
            sb.append(indent.repeat(depth))
            sb.append("<$prefix:Node>${escapeXml(it)}</$prefix:Node>")
            if (pretty) sb.appendLine()
        }
        role?.let {
            sb.append(indent.repeat(depth))
            sb.append("<$prefix:Role>${escapeXml(it)}</$prefix:Role>")
            if (pretty) sb.appendLine()
        }
        serializeDetail(sb, prefix, usePrefixedDetail = true, pretty, indent, depth)
    }

    private fun serializeSubcodes(sb: StringBuilder, subcodes: List<String>, prefix: String, pretty: Boolean, indent: String, depth: Int) {
        if (subcodes.isEmpty()) return
        sb.append(indent.repeat(depth))
        sb.append("<$prefix:Subcode>")
        if (pretty) sb.appendLine()
        sb.append(indent.repeat(depth + 1))
        sb.append("<$prefix:Value>${escapeXml(subcodes.first())}</$prefix:Value>")
        if (pretty) sb.appendLine()
        serializeSubcodes(sb, subcodes.drop(1), prefix, pretty, indent, depth + 1)
        sb.append(indent.repeat(depth))
        sb.append("</$prefix:Subcode>")
        if (pretty) sb.appendLine()
    }
}