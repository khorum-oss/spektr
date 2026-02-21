package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultCode
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultReason
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

/**
 * Abstract base class for SOAP fault builders.
 *
 * Provides common functionality for both SOAP 1.1 and 1.2 fault structures.
 * Subclasses implement version-specific fault element serialization.
 *
 * SOAP 1.1 faults use: `faultcode`, `faultstring`, `faultactor`, `detail`
 * SOAP 1.2 faults use: `Code`, `Reason`, `Node`, `Role`, `Detail`
 *
 * Methods for the wrong SOAP version will throw [IllegalStateException].
 */
sealed class SoapFaultBuilder : SoapBodyContent, SoapFaultScope {
    /** Optional detail element containing application-specific error information. */
    protected var detail: SoapElementBuilder? = null

    /**
     * Configures the fault detail section.
     *
     * @param block Configuration block for adding detail elements.
     */
    override fun detail(block: SoapElementBuilder.() -> Unit) {
        detail = SoapElementBuilder().apply(block)
    }

    // SOAP 1.1 methods - throw on SOAP 1.2
    override fun faultCode(code: String): Unit = versionMismatch("faultCode", "1.1")
    override fun faultString(reason: String): Unit = versionMismatch("faultString", "1.1")
    override fun faultActor(actor: String): Unit = versionMismatch("faultActor", "1.1")

    // SOAP 1.2 methods - throw on SOAP 1.1
    override fun code(value: String): Unit = versionMismatch("code", "1.2")
    override fun code(block: SoapFaultCode.() -> Unit): Unit = versionMismatch("code", "1.2")
    override fun reason(block: SoapFaultReason.() -> Unit): Unit = versionMismatch("reason", "1.2")
    override fun node(node: String): Unit = versionMismatch("node", "1.2")
    override fun role(role: String): Unit = versionMismatch("role", "1.2")

    private fun versionMismatch(method: String, requiredVersion: String): Nothing =
        throw IllegalStateException("$method requires SOAP $requiredVersion")

    /**
     * Serializes this fault to the given StringBuilder.
     *
     * @param sb The StringBuilder to append to.
     * @param prefix The SOAP envelope prefix for namespaced elements.
     * @param pretty Whether to format with indentation.
     * @param indent The indentation string.
     * @param depth The current nesting depth.
     */
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

    /**
     * Serializes the version-specific fault content.
     *
     * @param sb The StringBuilder to append to.
     * @param prefix The SOAP envelope prefix.
     * @param pretty Whether to format with indentation.
     * @param indent The indentation string.
     * @param depth The current nesting depth.
     */
    protected abstract fun serializeFaultContent(
        sb: StringBuilder,
        prefix: String, pretty: Boolean,
        indent: String, depth: Int
    )

    /**
     * Serializes the detail element if present.
     *
     * @param sb The StringBuilder to append to.
     * @param prefix The SOAP envelope prefix.
     * @param usePrefixedDetail If true, uses prefixed `Detail` (SOAP 1.2); otherwise `detail` (SOAP 1.1).
     * @param pretty Whether to format with indentation.
     * @param indent The indentation string.
     * @param depth The current nesting depth.
     */
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
