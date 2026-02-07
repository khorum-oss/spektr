package org.khorum.oss.spektr.dsl

data class SoapRequest(
    val headers: Map<String, List<String>>,
    val soapAction: String,
    val body: String?
)
