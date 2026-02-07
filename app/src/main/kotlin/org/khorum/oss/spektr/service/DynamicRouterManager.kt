package org.khorum.oss.spektr.service

import org.khorum.oss.spektr.dsl.DynamicRequest
import org.khorum.oss.spektr.dsl.DynamicResponse
import org.khorum.oss.spektr.dsl.EndpointDefinition
import org.khorum.oss.spektr.utils.Loggable
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
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicReference

@Component
@ConditionalOnProperty(name = ["spektr.rest.enabled"], havingValue = "true", matchIfMissing = true)
class DynamicRouterManager(
    private val objectMapper: ObjectMapper
) : Loggable {
    private val registry = AtomicReference<List<EndpointDefinition>>(emptyList())

    fun updateEndpoints(definitions: List<EndpointDefinition>) {
        registry.set(definitions)
    }

    // Single catch-all controller that dispatches to dynamic handlers
    @Bean
    fun dynamicRouterFunction(): RouterFunction<ServerResponse> {
        return RouterFunction { request ->
            log.debug("Incoming request: {} {}", request.method(), request.path())

            val matched = registry.get().find { endpoint ->
                endpoint.method.name == request.method().name()
                    && pathMatches(endpoint.path, request.path())
            }

            if (matched != null) {
                log.info("Matched endpoint: {} {} -> {}", matched.method, matched.path, request.path())
                Mono.just(HandlerFunction { req ->
                    req.bodyToMono(String::class.java)
                        .defaultIfEmpty("")
                        .flatMap { body ->
                            val dynamicReq = toDynamicRequest(req, matched.path, body.ifEmpty { null })

                            // Log path variables if present
                            if (dynamicReq.pathVariables.isNotEmpty()) {
                                log.info("Path variables: {}", dynamicReq.pathVariables)
                            }

                            // Log query params if present
                            if (dynamicReq.queryParams.isNotEmpty()) {
                                log.info("Query params: {}", dynamicReq.queryParams)
                            }

                            // Log request body if present
                            if (body.isNotEmpty()) {
                                log.info("Request body: {} bytes", body.length)
                                log.debug("Request body content:\n{}", body)
                            }

                            log.debug("Headers: {}", dynamicReq.headers.keys)

                            val dynamicResp = matched.handler.handle(dynamicReq)
                            log.info("Returning response: status={}, body type={}",
                                dynamicResp.status, dynamicResp.body?.javaClass?.simpleName ?: "null")
                            log.debug("Response body: {}", dynamicResp.body)

                            buildServerResponse(dynamicResp)
                        }
                })
            } else {
                log.debug("No matching endpoint for: {} {}", request.method(), request.path())
                Mono.empty()
            }
        }
    }

    private fun buildServerResponse(resp: DynamicResponse): Mono<ServerResponse> {
        val builder = ServerResponse.status(resp.status)
        resp.headers.forEach { (k, v) -> builder.header(k, v) }
        return when (val body = resp.body) {
            null -> builder.build()
            is String -> builder.contentType(MediaType.APPLICATION_JSON).bodyValue(body)
            else -> builder.contentType(MediaType.APPLICATION_JSON).bodyValue(objectMapper.writeValueAsString(body))
        }
    }

    private fun pathMatches(pattern: String, actual: String): Boolean {
        // Implement Ant-style or Spring PathPattern matching
        val pathPattern = PathPatternParser().parse(pattern)
        return pathPattern.matches(PathContainer.parsePath(actual))
    }

    private fun toDynamicRequest(req: ServerRequest, pattern: String, body: String? = null): DynamicRequest {
        val pathPattern = PathPatternParser().parse(pattern)
        val pathContainer = PathContainer.parsePath(req.path())
        val pathVars = pathPattern.matchAndExtract(pathContainer)
            ?.uriVariables ?: emptyMap()

        val headers = req.headers()
            .asHttpHeaders()
            .headerSet()
            .associate { entry -> entry.key to entry.value }

        return DynamicRequest(
            headers = headers,
            pathVariables = pathVars,
            queryParams = req.queryParams().toMap(),
            body = body?.ifEmpty { null }
        )
    }
}