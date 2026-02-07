package org.khorum.oss.spektr.dsl

data class EndpointDefinition(
    val method: HttpMethod,
    val path: String,
    val handler: DynamicHandler
)