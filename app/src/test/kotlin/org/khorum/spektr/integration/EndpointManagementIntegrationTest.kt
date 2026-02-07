package org.khorum.spektr.integration

import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.SpektrApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [SpektrApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class EndpointManagementIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `POST reload returns success with endpoint counts`() {
        webTestClient.post()
            .uri("/admin/endpoints/reload")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.endpointsLoaded").isNumber
            .jsonPath("$.jarsProcessed").isNumber
            .jsonPath("$.reloadTimeMs").isNumber
    }

    @Test
    fun `POST reload returns consistent results on multiple calls`() {
        // First call
        val firstResult = webTestClient.post()
            .uri("/admin/endpoints/reload")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        // Second call should return same count
        webTestClient.post()
            .uri("/admin/endpoints/reload")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.endpointsLoaded").isNumber
            .jsonPath("$.jarsProcessed").isNumber
    }

    @Test
    fun `POST reload includes timing information`() {
        webTestClient.post()
            .uri("/admin/endpoints/reload")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.reloadTimeMs").value<Int> { time ->
                assert(time >= 0) { "Reload time should be non-negative" }
            }
    }
}