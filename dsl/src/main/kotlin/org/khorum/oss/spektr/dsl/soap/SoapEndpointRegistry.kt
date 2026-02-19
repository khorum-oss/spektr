package org.khorum.oss.spektr.dsl.soap

class SoapEndpointRegistry {
    private val endpointList = mutableListOf<SoapEndpointDefinition>()
    val endpoints: List<SoapEndpointDefinition> get() = endpointList

    fun operation(path: String, soapAction: String, handler: SoapHandler) {
        endpointList.add(SoapEndpointDefinition(path, soapAction, handler))
    }
}

fun soapEndpoints(block: SoapEndpointRegistry.() -> Unit): SoapEndpointRegistry {
    return SoapEndpointRegistry().apply(block)
}
