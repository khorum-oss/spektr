package org.khorum.oss.spekter.examples.testcommon

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.JsonPathAssertions
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient

class TestClient(
    val webClient: WebTestClient,
    private val baseUri: String
) {
    private val self = this
    var body: Any? = null
    var expectation: Expectation = Expectation()
        private set

    fun withBody(body: Any) {
        this.body = body
    }

    fun expect(scope: Expectation.() -> Unit) {
        expectation.apply(scope)
    }

    fun get(additionalPathPart: String? = null, scope: TestClient.() -> Unit) {
        val config = self.apply(scope)

        val uri = config.baseUri.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("uri must be specified")

        val expect = config.expectation

        val fullUri = uri + (additionalPathPart ?: "")

        val callSetup = webClient
            .get()
            .uri(fullUri)

        val response = callSetup.exchange()

        val statusCheck = expect.getStatusCheck()

        val jsonPathChecks = expect.getJsonPathChecks()

        statusCheck(response.expectStatus())

        val jsonBody = response.expectBody()

        jsonPathChecks.forEach { it(jsonBody) }
    }

    fun post(additionalPathPart: String? = null, scope: TestClient.() -> Unit) {
        val config = self.apply(scope)

        val uri = config.baseUri.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("uri must be specified")

        val body = config.body

        if (body == null) {
            println("WARN: No body specified for POST request to $uri")
        }

        val expect = config.expectation

        val fullUri = uri + (additionalPathPart ?: "")

        val callSetup = webClient
            .post()
            .uri(fullUri)
            .contentType(MediaType.APPLICATION_JSON)

        val response = body
            ?.let { callSetup.bodyValue(it).exchange() }
            ?: callSetup.exchange()

        val statusCheck = expect.getStatusCheck()

        val jsonPathChecks = expect.getJsonPathChecks()

        statusCheck(response.expectStatus())

        val jsonBody = response.expectBody()

        jsonPathChecks.forEach { it(jsonBody) }
    }

    class Expectation {
        private var statusCheck: (StatusAssertions) -> Unit = {}
        private var jsonPathExecutions: MutableList<(WebTestClient.BodyContentSpec) -> Unit> = mutableListOf()

        fun withStatus(check: (StatusAssertions) -> Unit) {
            statusCheck = check
        }

        fun getJsonPathChecks(): List<(WebTestClient.BodyContentSpec) -> Unit> {
            return jsonPathExecutions
        }

        fun hasCreatedStatus() {
            withStatus { it.isCreated }
        }

        fun hasOkStatus() {
            withStatus { it.isOk }
        }

        fun getStatusCheck(): (StatusAssertions) -> Unit {
            return statusCheck
        }

        fun String.jsonPathValueEquals(expected: Any) {
            jsonPathExecutions.add { it.jsonPath(this).isEqualTo(expected) }
        }

        fun String.jsonPathValueExists() {
            jsonPathExecutions.add { it.jsonPath(this).exists() }
        }

        fun String.jsonPath(scope: JsonPathAssertions.() -> Unit) {
            jsonPathExecutions.add { scope(it.jsonPath(this)) }
        }
    }
}

fun configureShared(
    webClient: WebTestClient,
    uri: String
): TestClient {
    return TestClient(webClient, uri)
}