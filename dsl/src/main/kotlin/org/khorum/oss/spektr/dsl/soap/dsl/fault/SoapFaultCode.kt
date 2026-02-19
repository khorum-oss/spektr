package org.khorum.oss.spektr.dsl.soap.dsl.fault

class SoapFaultCode {
    private var value: String? = null
    private val subcodes: MutableList<String> = mutableListOf()

    // Internal getters for serialization
    internal fun getValue(): String? = value
    internal fun getSubcodes(): List<String> = subcodes

    fun value(code: String) { value = code }
    fun value(namespace: String, code: String) = value("$namespace:$code")
    fun subcode(code: String) { subcodes.add(code) }
    fun subcode(namespace: String, code: String) = subcode("$namespace:$code")
}