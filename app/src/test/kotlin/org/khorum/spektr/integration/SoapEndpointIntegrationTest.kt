package org.khorum.spektr.integration

import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.SpektrApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [SpektrApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SoapEndpointIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `SOAP endpoint returns correct response for ListGhosts action`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <ListGhosts xmlns="http://org.khorum-oss.com/ghost-book"/>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws")
            .header("SOAPAction", "\"ListGhosts\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_XML)
            .expectBody(String::class.java)
            .value {
                val body = requireNotNull(it)
                assert(body.contains("listGhostsResponse")) { "Response should contain listGhostsResponse element" }
                assert(body.contains("Poltergeist")) { "Response should contain ghost type" }
            }
    }

    @Test
    fun `SOAP endpoint returns correct response for GetGhost action`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <GetGhost xmlns="http://org.khorum-oss.com/ghost-book">
                  <type>OBAKE</type>
                </GetGhost>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws")
            .header("SOAPAction", "\"GetGhost\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_XML)
            .expectBody(String::class.java)
            .value {
                val body = requireNotNull(it)
                assert(body.contains("getGhostResponse")) { "Response should contain getGhostResponse element" }
                assert(body.contains("OBAKE")) { "Response should contain ghost type" }
                assert(body.contains("Nippon")) { "Response should contain origin" }
            }
    }

    @Test
    fun `SOAP endpoint with unknown action returns 404`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <UnknownAction/>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws")
            .header("SOAPAction", "\"UnknownAction\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `SOAP endpoint with wrong path returns 404`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <ListGhosts xmlns="http://org.khorum-oss.com/ghost-book"/>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws/nonexistent")
            .header("SOAPAction", "\"ListGhosts\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `REST endpoints still work alongside SOAP`() {
        webTestClient.get()
            .uri("/haunted-houses/01bd32f5-325f-41d3-8047-4812d197a183")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("01bd32f5-325f-41d3-8047-4812d197a183")
            .jsonPath("$.address.city").isEqualTo("Amityville")
    }

    @Test
    fun `reload includes SOAP endpoint counts`() {
        webTestClient.post()
            .uri("/admin/endpoints/reload")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.endpointsLoaded").isNumber
            .jsonPath("$.soapEndpointsLoaded").isNumber
            .jsonPath("$.jarsProcessed").isNumber
            .jsonPath("$.reloadTimeMs").isNumber
    }
}
