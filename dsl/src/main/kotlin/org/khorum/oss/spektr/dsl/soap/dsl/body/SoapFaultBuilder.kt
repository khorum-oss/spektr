package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultCode
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultReason
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

// ── Sealed hierarchy: what gets built ──────────────────
sealed class SoapFaultBuilder : SoapBodyContent, SoapFaultScope {
    protected var detail: SoapElementBuilder? = null

    override fun detail(block: SoapElementBuilder.() -> Unit) {
        detail = SoapElementBuilder().apply(block)
    }

    // Default no-ops — overridden by the relevant subclass
    override fun faultCode(code: String): Unit = versionMismatch("faultCode", "1.1")
    override fun faultString(reason: String): Unit = versionMismatch("faultString", "1.1")
    override fun faultActor(actor: String): Unit = versionMismatch("faultActor", "1.1")
    override fun code(value: String): Unit = versionMismatch("code", "1.2")
    override fun code(block: SoapFaultCode.() -> Unit): Unit = versionMismatch("code", "1.2")
    override fun reason(block: SoapFaultReason.() -> Unit): Unit = versionMismatch("reason", "1.2")
    override fun node(node: String): Unit = versionMismatch("node", "1.2")
    override fun role(role: String): Unit = versionMismatch("role", "1.2")

    private fun versionMismatch(method: String, requiredVersion: String): Nothing =
        throw IllegalStateException("$method requires SOAP $requiredVersion")

    internal fun serialize(
        sb: StringBuilder, 
        prefix: String, pretty: Boolean, 
        indent: String, depth: Int
    ) {
        sb.append(indent.repeat(depth))
        sb.append("<$prefix:Fault>")
        if (pretty) sb.appendLine()
        serializeFaultContent(sb, prefix, pretty, indent, depth + 1)
        sb.append(indent.repeat(depth))
        sb.append("</$prefix:Fault>")
        if (pretty) sb.appendLine()
    }

    protected abstract fun serializeFaultContent(
        sb: StringBuilder, 
        prefix: String, pretty: Boolean, 
        indent: String, depth: Int
    )

    protected fun serializeDetail(
        sb: StringBuilder, 
        prefix: String, usePrefixedDetail: Boolean, 
        pretty: Boolean, 
        indent: String, depth: Int
    ) {
        detail?.let { d ->
            val detailTag = if (usePrefixedDetail) "$prefix:Detail" else "detail"
            sb.append(indent.repeat(depth))
            sb.append("<$detailTag>")
            if (pretty) sb.appendLine()
            d.serializeContent(sb, pretty, indent, depth + 1)
            sb.append(indent.repeat(depth))
            sb.append("</$detailTag>")
            if (pretty) sb.appendLine()
        }
    }
}