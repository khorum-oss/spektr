package org.khorum.oss.spektr.dsl.soap.dsl

/**
 * Builder for configuring XML namespaces in a SOAP envelope.
 *
 * Provides methods to add namespace prefix-to-URI mappings that will be
 * rendered as xmlns attributes on the envelope element.
 *
 * Example:
 * ```kotlin
 * namespaces {
 *     ns("xmlns:ns", "http://example.com/api")
 *     ns("xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance")
 * }
 * ```
 */
class SoapNamespacesBuilder {
    private val namespaces: MutableMap<String, String> = mutableMapOf()

    /**
     * Returns the configured namespace mappings.
     *
     * @return Map of namespace attributes to their URI values.
     */
    internal fun getNamespaces(): Map<String, String> = namespaces

    /**
     * Adds a namespace declaration.
     *
     * @param prefix The xmlns attribute (e.g., "xmlns:ns" or "xmlns:xsi").
     * @param uri The namespace URI.
     */
    fun ns(prefix: String, uri: String) {
        namespaces[prefix] = uri
    }

    /**
     * Adds a namespace declaration from a pair.
     *
     * @param entry A pair of (prefix, uri).
     */
    fun ns(entry: Pair<String, String>) = ns(entry.first, entry.second)
}
