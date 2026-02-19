package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

class Soap11FaultBuilder : SoapFaultBuilder() {
    private var faultCode: String? = null
    private var faultString: String? = null
    private var faultActor: String? = null

    override fun faultCode(code: String) { faultCode = code }
    override fun faultString(reason: String) { faultString = reason }
    override fun faultActor(actor: String) { faultActor = actor }

    override fun serializeFaultContent(sb: StringBuilder, prefix: String, pretty: Boolean, indent: String, depth: Int) {
        faultCode?.let {
            sb.append(indent.repeat(depth))
            sb.append("<faultcode>${escapeXml(it)}</faultcode>")
            if (pretty) sb.appendLine()
        }
        faultString?.let {
            sb.append(indent.repeat(depth))
            sb.append("<faultstring>${escapeXml(it)}</faultstring>")
            if (pretty) sb.appendLine()
        }
        faultActor?.let {
            sb.append(indent.repeat(depth))
            sb.append("<faultactor>${escapeXml(it)}</faultactor>")
            if (pretty) sb.appendLine()
        }
        serializeDetail(sb, prefix, usePrefixedDetail = false, pretty, indent, depth)
    }
}