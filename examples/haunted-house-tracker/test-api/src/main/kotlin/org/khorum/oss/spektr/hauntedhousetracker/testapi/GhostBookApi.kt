package org.khorum.oss.spektr.hauntedhousetracker.testapi

import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.SoapEndpointRegistry
import org.khorum.oss.spektr.dsl.SoapResponse

class GhostBookApi : EndpointModule {
    override fun SoapEndpointRegistry.configureSoap() {
        // Create ghost - use ns: prefix for root only (UNQUALIFIED element form)
        operation("/ws", "CreateGhost") { request ->
            val type = extractElement(request.body, "type") ?: "Unknown"
            val origin = extractElement(request.body, "origin")

            SoapResponse(
                body = """
                    |<ns:createGhostResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                    |  <ghost>
                    |    <type>$type</type>
                    |    ${origin?.let { "<origin>$it</origin>" } ?: ""}
                    |  </ghost>
                    |</ns:createGhostResponse>
                """.trimMargin()
            )
        }

        // Get ghost by type
        operation("/ws", "GetGhost") { request ->
            val type = extractElement(request.body, "type") ?: "Unknown"

            val responseBody = if (type.uppercase() == "OBAKE") {
                """
                    |<ns:getGhostResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                    |  <ghost>
                    |    <type>$type</type>
                    |    <origin>Nippon</origin>
                    |  </ghost>
                    |</ns:getGhostResponse>
                """.trimMargin()
            } else {
                """
                    |<ns:getGhostResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                    |</ns:getGhostResponse>
                """.trimMargin()
            }

            SoapResponse(body = responseBody)
        }

        // List all ghosts
        operation("/ws", "ListGhosts") { request ->
            SoapResponse(
                body = """
                    |<ns:listGhostsResponse xmlns:ns="http://org.khorum-oss.com/ghost-book">
                    |  <ghost><type>Poltergeist</type></ghost>
                    |  <ghost><type>Yurei</type></ghost>
                    |</ns:listGhostsResponse>
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