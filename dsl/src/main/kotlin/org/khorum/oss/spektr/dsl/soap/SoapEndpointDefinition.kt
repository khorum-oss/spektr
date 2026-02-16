package org.khorum.oss.spektr.dsl.soap

data class SoapEndpointDefinition(
    val path: String,
    val soapAction: String,
    val handler: SoapHandler
)
