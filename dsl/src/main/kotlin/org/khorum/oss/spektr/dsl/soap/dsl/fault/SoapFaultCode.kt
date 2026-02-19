package org.khorum.oss.spektr.dsl.soap.dsl.fault

/**
 * Builder for SOAP 1.2 fault codes.
 *
 * Supports hierarchical fault codes with a primary value and optional subcodes.
 * Subcodes can be nested to provide increasingly specific error information.
 *
 * Example:
 * ```kotlin
 * code {
 *     value("env:Sender")
 *     subcode("ns:ValidationError")
 *     subcode("ns:MissingField")
 * }
 * // Produces:
 * // <env:Code>
 * //   <env:Value>env:Sender</env:Value>
 * //   <env:Subcode>
 * //     <env:Value>ns:ValidationError</env:Value>
 * //     <env:Subcode>
 * //       <env:Value>ns:MissingField</env:Value>
 * //     </env:Subcode>
 * //   </env:Subcode>
 * // </env:Code>
 * ```
 */
class SoapFaultCode {
    private var value: String? = null
    private val subcodes: MutableList<String> = mutableListOf()

    /**
     * Returns the primary fault code value.
     *
     * @return The value, or null if not set.
     */
    internal fun getValue(): String? = value

    /**
     * Returns the list of subcodes.
     *
     * @return The subcodes in order of nesting depth.
     */
    internal fun getSubcodes(): List<String> = subcodes

    /**
     * Sets the primary fault code value.
     *
     * Standard values include:
     * - `env:VersionMismatch` - Invalid SOAP version
     * - `env:MustUnderstand` - Required header not understood
     * - `env:DataEncodingUnknown` - Unsupported encoding
     * - `env:Sender` - Client-side error
     * - `env:Receiver` - Server-side error
     *
     * @param code The fault code value.
     */
    fun value(code: String) { value = code }

    /**
     * Sets the primary fault code value with a namespace prefix.
     *
     * @param namespace The namespace prefix.
     * @param code The local code name.
     */
    fun value(namespace: String, code: String) = value("$namespace:$code")

    /**
     * Adds a subcode to the fault code hierarchy.
     *
     * Subcodes are nested in order of addition, providing increasingly
     * specific error categorization.
     *
     * @param code The subcode value.
     */
    fun subcode(code: String) { subcodes.add(code) }

    /**
     * Adds a subcode with a namespace prefix.
     *
     * @param namespace The namespace prefix.
     * @param code The local code name.
     */
    fun subcode(namespace: String, code: String) = subcode("$namespace:$code")
}
