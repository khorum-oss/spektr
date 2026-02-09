package org.khorum.oss.spektr.hauntedhousetracker.testapi

import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.EndpointRegistry
import org.khorum.oss.spektr.dsl.SoapEndpointRegistry
import org.khorum.oss.spektr.dsl.SoapResponse

class GhostApi : EndpointModule {
    override fun EndpointRegistry.configure() {

    }

    override fun SoapEndpointRegistry.configureSoap() {
        operation("/ws", "GetGhost") { request ->
            val type = extractElement(request.body, "type") ?: "Unknown"
            SoapResponse(
                body = """
                    |<GetGhostResponse>
                    |  <ghost>
                    |    <type>$type</type>
                    |  </ghost>
                    |</GetGhostResponse>
                """.trimMargin()
            )
        }
    }

    private fun extractElement(xml: String?, elementName: String): String? {
        if (xml == null) return null
        val regex = Regex("<$elementName[^>]*>([^<]*)</$elementName>")
        return regex.find(xml)?.groupValues?.get(1)
    }
}