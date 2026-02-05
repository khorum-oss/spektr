package org.khorum.oss.spektr.dsl

data class DynamicResponse(
    val status: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null
)