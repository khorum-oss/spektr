package org.khorum.oss.spektr.dsl.soap.dsl

class SoapNamespacesBuilder {
    private val namespaces: MutableMap<String, String> = mutableMapOf()

    // Internal getter for serialization
    internal fun getNamespaces(): Map<String, String> = namespaces

    fun ns(prefix: String, uri: String) {
        namespaces[prefix] = uri
    }

    fun ns(entry: Pair<String, String>) = ns(entry.first, entry.second)
}