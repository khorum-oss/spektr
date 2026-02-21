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
        // Uses pre-seeded UUID from HauntedHouseTrackerApi
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
    fun `GET endpoint works with different path variables`() {
        // Uses second pre-seeded UUID from HauntedHouseTrackerApi
        webTestClient.get()
            .uri("/haunted-houses/fa5ea8b6-e545-4b79-9310-da92fc04365f")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("fa5ea8b6-e545-4b79-9310-da92fc04365f")
            .jsonPath("$.address.city").isEqualTo("San Jose")
    }

    @Test
    fun `GET all houses returns list`() {
        webTestClient.get()
            .uri("/haunted-houses")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(3)
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
            .uri("/haunted-houses/01bd32f5-325f-41d3-8047-4812d197a183")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}
