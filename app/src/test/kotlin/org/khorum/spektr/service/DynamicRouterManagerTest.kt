package org.khorum.spektr.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.dsl.rest.DynamicResponse
import org.khorum.oss.spektr.dsl.rest.RestEndpointDefinition
import org.khorum.oss.spektr.dsl.rest.HttpMethod
import org.khorum.oss.spektr.service.DynamicRouterManager
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper

class DynamicRouterManagerTest {

    private lateinit var routerManager: DynamicRouterManager
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        routerManager = DynamicRouterManager(objectMapper)
    }

    @Test
    fun `updateEndpoints registers endpoints correctly`() {
        val endpoints = listOf(
            RestEndpointDefinition(HttpMethod.GET, "/test/{id}") { request ->
                DynamicResponse(body = mapOf("id" to request.pathVariables["id"]))
            }
        )

        routerManager.updateEndpoints(endpoints)

        val routerFunction = routerManager.dynamicRouterFunction()
        val request = MockServerHttpRequest.get("/test/123").build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `unregistered path returns empty`() {
        routerManager.updateEndpoints(emptyList())

        val routerFunction = routerManager.dynamicRouterFunction()
        val request = MockServerHttpRequest.get("/nonexistent").build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }

    @Test
    fun `wrong method returns empty`() {
        val endpoints = listOf(
            RestEndpointDefinition(HttpMethod.GET, "/test") { _ ->
                DynamicResponse(body = "ok")
            }
        )

        routerManager.updateEndpoints(endpoints)

        val routerFunction = routerManager.dynamicRouterFunction()
        val request = MockServerHttpRequest.post("/test").build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }
}