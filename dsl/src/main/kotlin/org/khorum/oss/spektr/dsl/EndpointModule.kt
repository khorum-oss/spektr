package org.khorum.oss.spektr.dsl

interface EndpointModule {
    fun EndpointRegistry.configure()

    fun SoapEndpointRegistry.configureSoap() {
        // Default no-op so existing modules don't need to implement SOAP
    }
}