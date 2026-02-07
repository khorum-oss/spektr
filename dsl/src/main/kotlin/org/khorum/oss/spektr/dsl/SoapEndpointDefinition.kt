package org.khorum.oss.spektr.dsl

data class SoapEndpointDefinition(
    val path: String,
    val soapAction: String,
    val handler: SoapHandler
)
