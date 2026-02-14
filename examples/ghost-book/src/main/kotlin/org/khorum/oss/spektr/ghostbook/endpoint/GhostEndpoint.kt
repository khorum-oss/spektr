package org.khorum.oss.spektr.ghostbook.endpoint

import kotlinx.coroutines.runBlocking
import org.khorum.oss.spekter.examples.common.domain.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.domain.CreateGhostResponse
import org.khorum.oss.spekter.examples.common.domain.GetGhostRequest
import org.khorum.oss.spekter.examples.common.domain.GetGhostResponse
import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.khorum.oss.spekter.examples.common.domain.ListGhostsRequest
import org.khorum.oss.spekter.examples.common.domain.ListGhostsResponse
import org.khorum.oss.spektr.ghostbook.service.GhostService
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload

@Endpoint
class GhostEndpoint(
    private val ghostService: GhostService
) {

    @ResponsePayload
    @PayloadRoot(namespace = Ghost.NAMESPACE, localPart = "createGhostRequest")
    fun createGhost(@RequestPayload request: CreateGhostRequest): CreateGhostResponse = runBlocking {
        ghostService.create(request)
    }

    @PayloadRoot(namespace = Ghost.NAMESPACE, localPart = "getGhostRequest")
    @ResponsePayload
    fun getGhost(@RequestPayload request: GetGhostRequest): GetGhostResponse? = runBlocking {
        ghostService.findByType(request)
    }

    @PayloadRoot(namespace = Ghost.NAMESPACE, localPart = "listGhostsRequest")
    @ResponsePayload
    fun listGhosts(@RequestPayload request: ListGhostsRequest): ListGhostsResponse = runBlocking {
        val ghosts = ghostService.findAll(request.includeHouses)
        ListGhostsResponse(ghosts = ghosts.toList())
    }
}