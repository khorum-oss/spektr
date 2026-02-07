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
        // Uses pre-seeded UUID from HouseEndpoints
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
    fun `GET endpoint works with different path variables`() {
        // Uses second pre-seeded UUID from HouseEndpoints
        webTestClient.get()
            .uri("/api/houses/7a99c0dc-64cf-4e0a-948c-1d2a6a191f30")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("7a99c0dc-64cf-4e0a-948c-1d2a6a191f30")
            .jsonPath("$.isHaunted").isEqualTo(true)
    }

    @Test
    fun `GET all houses returns list`() {
        webTestClient.get()
            .uri("/api/house")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(2)
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
            .uri("/api/houses/dbf40fb3-e1bd-4683-8a78-547f054e4d42")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}