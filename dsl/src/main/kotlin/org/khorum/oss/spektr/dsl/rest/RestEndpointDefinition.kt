package org.khorum.oss.spektr.dsl.rest

data class RestEndpointDefinition(
    val method: HttpMethod,
    val path: String,
    val handler: DynamicHandler
)