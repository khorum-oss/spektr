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
    fun `SOAP endpoint returns correct response for GetWeather action`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <GetWeather xmlns="http://spektr.khorum.org/weather">
                  <city>Seattle</city>
                </GetWeather>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws/weather")
            .header("SOAPAction", "\"GetWeather\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_XML)
            .expectBody(String::class.java)
            .value {
                val body = requireNotNull(it)
                assert(body.contains("Seattle")) { "Response should contain city name" }
                assert(body.contains("GetWeatherResponse")) { "Response should contain GetWeatherResponse element" }
                assert(body.contains("72")) { "Response should contain temperature" }
            }
    }

    @Test
    fun `SOAP fault endpoint returns 500 with fault envelope`() {
        val soapRequest = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <InvalidOperation xmlns="http://spektr.khorum.org/weather"/>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws/weather")
            .header("SOAPAction", "\"InvalidOperation\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(String::class.java)
            .value {
                val body = requireNotNull(it)
                assert(body.contains("Fault")) { "Response should contain Fault element" }
                assert(body.contains("soap:Client")) { "Response should contain fault code" }
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
            .uri("/ws/weather")
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
                <GetWeather xmlns="http://spektr.khorum.org/weather">
                  <city>Seattle</city>
                </GetWeather>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        webTestClient.post()
            .uri("/ws/nonexistent")
            .header("SOAPAction", "\"GetWeather\"")
            .contentType(MediaType.TEXT_XML)
            .bodyValue(soapRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `REST endpoints still work alongside SOAP`() {
        webTestClient.get()
            .uri("/api/houses/dbf40fb3-e1bd-4683-8a78-547f054e4d42")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("dbf40fb3-e1bd-4683-8a78-547f054e4d42")
            .jsonPath("$.isHaunted").isEqualTo(true)
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
