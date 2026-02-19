package org.khorum.oss.spektr.dsl.soap.dsl.fault

import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker
import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementBuilder

/**
 * Interface defining the DSL scope for configuring SOAP faults.
 *
 * This interface provides methods for both SOAP 1.1 and 1.2 fault structures.
 * When using the wrong method for the configured SOAP version, an
 * [IllegalStateException] will be thrown at runtime.
 *
 * **SOAP 1.1 methods:**
 * - [faultCode] - Sets the fault code (e.g., "soap:Server")
 * - [faultString] - Sets the fault description
 * - [faultActor] - Sets the actor URI
 *
 * **SOAP 1.2 methods:**
 * - [code] - Sets the fault code with optional subcodes
 * - [reason] - Sets the fault reason with language tag
 * - [node] - Sets the node URI
 * - [role] - Sets the role URI
 *
 * **Common to both versions:**
 * - [detail] - Adds application-specific error details
 */
interface SoapFaultScope {
    /**
     * Configures the fault detail section (both SOAP versions).
     *
     * @param block Configuration block for adding detail elements.
     */
    fun detail(block: SoapElementBuilder.() -> Unit)

    // SOAP 1.1 methods

    /**
     * Sets the SOAP 1.1 fault code.
     *
     * @param code The fault code value (e.g., "soap:Server", "soap:Client").
     * @throws IllegalStateException if called on a SOAP 1.2 envelope.
     */
    fun faultCode(code: String)

    /**
     * Sets the SOAP 1.1 fault string (human-readable description).
     *
     * @param reason The fault description.
     * @throws IllegalStateException if called on a SOAP 1.2 envelope.
     */
    fun faultString(reason: String)

    /**
     * Sets the SOAP 1.1 fault actor.
     *
     * @param actor The URI of the actor that caused the fault.
     * @throws IllegalStateException if called on a SOAP 1.2 envelope.
     */
    fun faultActor(actor: String)

    // SOAP 1.2 methods

    /**
     * Sets the SOAP 1.2 fault code using a simple string value.
     *
     * @param value The fault code (e.g., "env:Sender", "env:Receiver").
     * @throws IllegalStateException if called on a SOAP 1.1 envelope.
     */
    fun code(value: String)

    /**
     * Configures the SOAP 1.2 fault code with optional subcodes.
     *
     * @param block Configuration block for the fault code.
     * @throws IllegalStateException if called on a SOAP 1.1 envelope.
     */
    @SoapDslMarker
    fun code(block: SoapFaultCode.() -> Unit)

    /**
     * Configures the SOAP 1.2 fault reason.
     *
     * @param block Configuration block for the reason text and language.
     * @throws IllegalStateException if called on a SOAP 1.1 envelope.
     */
    @SoapDslMarker
    fun reason(block: SoapFaultReason.() -> Unit)

    /**
     * Sets the SOAP 1.2 fault node.
     *
     * @param node The URI of the SOAP node that generated the fault.
     * @throws IllegalStateException if called on a SOAP 1.1 envelope.
     */
    fun node(node: String)

    /**
     * Sets the SOAP 1.2 fault role.
     *
     * @param role The URI of the role the node was operating in.
     * @throws IllegalStateException if called on a SOAP 1.1 envelope.
     */
    fun role(role: String)
}
