package org.khorum.oss.spektr.dsl.soap

data class SoapRequest(
    val headers: Map<String, List<String>>,
    val soapAction: String,
    val body: String?
)
