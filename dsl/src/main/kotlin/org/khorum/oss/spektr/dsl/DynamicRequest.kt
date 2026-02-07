package org.khorum.oss.spektr.dsl

data class DynamicRequest(
    val headers: Map<String, List<String>>,
    val pathVariables: Map<String, String>,
    val queryParams: Map<String, List<String>>,
    val body: String?
)