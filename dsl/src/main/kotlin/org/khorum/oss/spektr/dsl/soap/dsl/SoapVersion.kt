package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.body.Soap11FaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.Soap12FaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapFaultBuilder

/**
 * Enumeration of SOAP protocol versions.
 *
 * Each version has specific namespace URIs, fault structures, and serialization rules.
 * The version determines which fault builder is used and the envelope namespace.
 */
enum class SoapVersion {
    /**
     * SOAP 1.1 version.
     *
     * Uses namespace: `http://schemas.xmlsoap.org/soap/envelope/`
     * Fault structure uses `faultcode`, `faultstring`, `faultactor`, and `detail` elements.
     */
    V1_1 {
        override fun faultBuilder() = Soap11FaultBuilder()
    },

    /**
     * SOAP 1.2 version.
     *
     * Uses namespace: `http://www.w3.org/2003/05/soap-envelope`
     * Fault structure uses prefixed elements: `Code`, `Reason`, `Node`, `Role`, and `Detail`.
     */
    V1_2 {
        override fun faultBuilder() = Soap12FaultBuilder()
    };

    /**
     * Creates a fault builder appropriate for this SOAP version.
     *
     * @return A new [SoapFaultBuilder] instance for this version.
     */
    abstract fun faultBuilder(): SoapFaultBuilder
}
