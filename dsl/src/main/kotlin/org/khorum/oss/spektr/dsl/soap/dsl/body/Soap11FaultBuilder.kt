package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.escapeXml

/**
 * SOAP 1.1 fault builder.
 *
 * Builds fault elements according to the SOAP 1.1 specification using
 * unprefixed child elements: `faultcode`, `faultstring`, `faultactor`, and `detail`.
 *
 * Example:
 * ```kotlin
 * fault {
 *     faultCode("soap:Server")
 *     faultString("Internal server error")
 *     faultActor("http://example.com/service")
 *     detail {
 *         element("errorCode") { content = "ERR_500" }
 *     }
 * }
 * ```
 */
class Soap11FaultBuilder : SoapFaultBuilder() {
    private var faultCode: String? = null
    private var faultString: String? = null
    private var faultActor: String? = null

    /**
     * Sets the fault code.
     *
     * Common values include `soap:Server` for server errors and `soap:Client` for client errors.
     *
     * @param code The fault code value.
     */
    override fun faultCode(code: String) { faultCode = code }

    /**
     * Sets the human-readable fault description.
     *
     * @param reason The fault message.
     */
    override fun faultString(reason: String) { faultString = reason }

    /**
     * Sets the URI of the actor that caused the fault.
     *
     * @param actor The actor URI.
     */
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
