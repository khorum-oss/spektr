package org.khorum.oss.spektr.service

import org.khorum.oss.spektr.dsl.SoapEndpointDefinition
import org.khorum.oss.spektr.dsl.SoapRequest
import org.khorum.oss.spektr.dsl.SoapResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicReference

@Component
@ConditionalOnProperty(name = ["spektr.soap.enabled"], havingValue = "true", matchIfMissing = true)
class SoapRouterManager {
    private val registry = AtomicReference<List<SoapEndpointDefinition>>(emptyList())

    companion object {
        val TEXT_XML: MediaType = MediaType.parseMediaType("text/xml;charset=UTF-8")
    }

    fun updateEndpoints(definitions: List<SoapEndpointDefinition>) {
        registry.set(definitions)
    }

    @Bean
    fun soapRouterFunction(): RouterFunction<ServerResponse> {
        return RouterFunction { request ->
            if (!isSoapRequest(request)) return@RouterFunction Mono.empty()

            val soapAction = extractSoapAction(request)
            val matched = registry.get().find { endpoint ->
                pathMatches(endpoint.path, request.path()) && endpoint.soapAction == soapAction
            }

            if (matched != null) {
                Mono.just(HandlerFunction { req ->
                    req.bodyToMono(String::class.java)
                        .defaultIfEmpty("")
                        .flatMap { body ->
                            val soapReq = toSoapRequest(req, soapAction, body)
                            val soapResp = matched.handler.handle(soapReq)
                            buildSoapResponse(soapResp)
                        }
                })
            } else {
                Mono.empty()
            }
        }
    }

    private fun isSoapRequest(request: ServerRequest): Boolean {
        val contentType = request.headers().contentType().orElse(null)
        val hasSoapContentType = contentType != null && (
            contentType.isCompatibleWith(MediaType.TEXT_XML) ||
                contentType.isCompatibleWith(MediaType.parseMediaType("application/soap+xml"))
            )
        val hasSoapAction = request.headers().firstHeader("SOAPAction") != null
        return hasSoapContentType || hasSoapAction
    }

    private fun extractSoapAction(request: ServerRequest): String {
        return request.headers().firstHeader("SOAPAction")
            ?.trim('"')
            ?: ""
    }

    private fun buildSoapResponse(resp: SoapResponse): Mono<ServerResponse> {
        val builder = ServerResponse.status(resp.status)
        resp.headers.forEach { (k, v) -> builder.header(k, v) }
        return when (val body = resp.body) {
            null -> builder.contentType(TEXT_XML).build()
            else -> builder.contentType(TEXT_XML).bodyValue(body)
        }
    }

    private fun pathMatches(pattern: String, actual: String): Boolean {
        val pathPattern = PathPatternParser().parse(pattern)
        return pathPattern.matches(PathContainer.parsePath(actual))
    }

    private fun toSoapRequest(req: ServerRequest, soapAction: String, body: String): SoapRequest {
        val headers = req.headers()
            .asHttpHeaders()
            .headerSet()
            .associate { entry -> entry.key to entry.value }

        return SoapRequest(
            headers = headers,
            soapAction = soapAction,
            body = body.ifEmpty { null }
        )
    }
}
