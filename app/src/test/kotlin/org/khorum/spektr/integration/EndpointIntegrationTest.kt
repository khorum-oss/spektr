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
class EndpointIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `GET endpoint returns correct response with path variable`() {
        webTestClient.get()
            .uri("/api/house/123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("123")
            .jsonPath("$.name").isEqualTo("Haunted House 123")
    }

    @Test
    fun `GET endpoint works with different path variables`() {
        webTestClient.get()
            .uri("/api/house/abc-456")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("abc-456")
            .jsonPath("$.name").isEqualTo("Haunted House abc-456")
    }

    @Test
    fun `non-existent endpoint returns 404`() {
        webTestClient.get()
            .uri("/api/nonexistent/123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `wrong HTTP method returns 404`() {
        webTestClient.post()
            .uri("/api/house/123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}