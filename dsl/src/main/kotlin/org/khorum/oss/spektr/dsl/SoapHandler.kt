package org.khorum.oss.spektr.dsl

fun interface SoapHandler {
    fun handle(request: SoapRequest): SoapResponse
}
