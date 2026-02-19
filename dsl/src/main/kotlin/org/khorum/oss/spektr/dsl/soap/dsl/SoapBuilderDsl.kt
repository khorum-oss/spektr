package org.khorum.oss.spektr.dsl.soap.dsl

@SoapDslMarker
fun soapEnvelope(block: SoapEnvelopeBuilder.() -> Unit): SoapEnvelopeBuilder {
    return SoapEnvelopeBuilder().apply(block)
}