package org.khorum.oss.spektr.dsl

import org.khorum.oss.spektr.dsl.rest.RestEndpointRegistry
import org.khorum.oss.spektr.dsl.soap.SoapEndpointRegistry

interface EndpointModule {
    fun RestEndpointRegistry.configure() {

    }

    fun SoapEndpointRegistry.configureSoap() {
        // Default no-op so existing modules don't need to implement SOAP
    }
}