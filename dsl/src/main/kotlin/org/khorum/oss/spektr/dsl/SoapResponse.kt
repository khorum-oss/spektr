package org.khorum.oss.spektr.dsl

data class SoapResponse(
    val status: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)
