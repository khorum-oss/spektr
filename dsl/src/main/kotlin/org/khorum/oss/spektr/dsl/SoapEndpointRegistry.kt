package org.khorum.oss.spektr.dsl

class SoapEndpointRegistry {
    private val endpointList = mutableListOf<SoapEndpointDefinition>()
    val endpoints: List<SoapEndpointDefinition> get() = endpointList

    fun operation(path: String, soapAction: String, handler: SoapHandler) {
        endpointList.add(SoapEndpointDefinition(path, soapAction, handler))
    }

    fun soapFault(path: String, soapAction: String, faultCode: String, faultString: String) {
        endpointList.add(SoapEndpointDefinition(path, soapAction) { _ ->
            SoapResponse(
                status = 500,
                body = """
                    |<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    |  <soap:Body>
                    |    <soap:Fault>
                    |      <faultcode>$faultCode</faultcode>
                    |      <faultstring>$faultString</faultstring>
                    |    </soap:Fault>
                    |  </soap:Body>
                    |</soap:Envelope>
                """.trimMargin()
            )
        })
    }
}

fun soapEndpoints(block: SoapEndpointRegistry.() -> Unit): SoapEndpointRegistry {
    return SoapEndpointRegistry().apply(block)
}
