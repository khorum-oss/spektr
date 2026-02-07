package org.khorum.spektr.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.dsl.SoapEndpointDefinition
import org.khorum.oss.spektr.dsl.SoapResponse
import org.khorum.oss.spektr.service.SoapRouterManager
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.test.StepVerifier

class SoapRouterManagerTest {

    private lateinit var soapRouterManager: SoapRouterManager

    @BeforeEach
    fun setUp() {
        soapRouterManager = SoapRouterManager()
    }

    @Test
    fun `SOAP request with matching action routes correctly`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("SOAPAction", "\"TestAction\"")
            .header("Content-Type", "text/xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `non-SOAP request returns empty`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("Content-Type", "application/json")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }

    @Test
    fun `wrong SOAP action returns empty`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("SOAPAction", "\"WrongAction\"")
            .header("Content-Type", "text/xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }

    @Test
    fun `wrong path returns empty`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/other")
            .header("SOAPAction", "\"TestAction\"")
            .header("Content-Type", "text/xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }

    @Test
    fun `empty registry returns empty for any SOAP request`() {
        soapRouterManager.updateEndpoints(emptyList())

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("SOAPAction", "\"TestAction\"")
            .header("Content-Type", "text/xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .verifyComplete()
    }

    @Test
    fun `SOAP action without quotes is matched`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("SOAPAction", "TestAction")
            .header("Content-Type", "text/xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `application soap+xml content type is recognized`() {
        val endpoints = listOf(
            SoapEndpointDefinition("/ws/test", "TestAction") { _ ->
                SoapResponse(body = "<TestResponse>OK</TestResponse>")
            }
        )

        soapRouterManager.updateEndpoints(endpoints)

        val routerFunction = soapRouterManager.soapRouterFunction()
        val request = MockServerHttpRequest.post("/ws/test")
            .header("SOAPAction", "\"TestAction\"")
            .header("Content-Type", "application/soap+xml")
            .build()
        val exchange = MockServerWebExchange.from(request)
        val serverRequest = ServerRequest.create(exchange, emptyList())

        StepVerifier.create(routerFunction.route(serverRequest))
            .expectNextCount(1)
            .verifyComplete()
    }
}
