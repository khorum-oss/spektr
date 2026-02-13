package org.khorum.oss.spektr.dsl

interface EndpointModule {
    fun RestEndpointRegistry.configure() {

    }

    fun SoapEndpointRegistry.configureSoap() {
        // Default no-op so existing modules don't need to implement SOAP
    }
}