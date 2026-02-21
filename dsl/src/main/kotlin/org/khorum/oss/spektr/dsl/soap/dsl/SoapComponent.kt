package org.khorum.oss.spektr.dsl.soap.dsl

/**
 * Interface for SOAP components that can be serialized to XML strings.
 *
 * Components implementing this interface support pretty-printed output
 * with configurable indentation.
 */
interface SoapComponent {
    /**
     * Serializes this component to a formatted XML string with indentation.
     *
     * @param indent The string to use for each indentation level (default: two spaces).
     * @return The pretty-printed XML representation.
     */
    fun toPrettyString(indent: String = "  "): String
}
