package org.khorum.oss.spektr.examples.base

import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.RestEndpointRegistry
import org.khorum.oss.spektr.dsl.SoapEndpointRegistry
import org.khorum.oss.spektr.dsl.SoapResponse

class WeatherSoapEndpoints : EndpointModule {
    override fun RestEndpointRegistry.configure() {
        // No REST endpoints in this module
    }

    override fun SoapEndpointRegistry.configureSoap() {
        operation("/ws/weather", "GetWeather") { request ->
            val city = extractElement(request.body, "city") ?: "Unknown"
            SoapResponse(
                body = """
                    |<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    |  <soap:Body>
                    |    <GetWeatherResponse xmlns="http://spektr.khorum.org/weather">
                    |      <city>$city</city>
                    |      <temperature>72</temperature>
                    |      <unit>F</unit>
                    |      <condition>Sunny</condition>
                    |    </GetWeatherResponse>
                    |  </soap:Body>
                    |</soap:Envelope>
                """.trimMargin()
            )
        }

        soapFault("/ws/weather", "InvalidOperation", "soap:Client", "Operation not supported")
    }

    private fun extractElement(xml: String?, elementName: String): String? {
        if (xml == null) return null
        val regex = Regex("<$elementName[^>]*>([^<]*)</$elementName>")
        return regex.find(xml)?.groupValues?.get(1)
    }
}
