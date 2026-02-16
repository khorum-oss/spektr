package org.khorum.oss.spektr.dsl.soap

fun interface SoapHandler {
    fun handle(request: SoapRequest): SoapResponse
}
