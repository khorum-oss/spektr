package org.khorum.oss.spekter.ghostbook.endpoint

import org.khorum.oss.spekter.examples.common.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.CreateGhostResponse
import org.khorum.oss.spekter.examples.common.GetGhostRequest
import org.khorum.oss.spekter.examples.common.GetGhostResponse
import org.khorum.oss.spekter.examples.common.Ghost
import org.khorum.oss.spekter.examples.common.ListGhostsRequest
import org.khorum.oss.spekter.examples.common.ListGhostsResponse
import org.khorum.oss.spekter.ghostbook.service.GhostService
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
    fun createGhost(@RequestPayload request: CreateGhostRequest): CreateGhostResponse {
        val ghost = ghostService.create(request)
        return CreateGhostResponse(ghost = ghost)
    }


    @PayloadRoot(namespace = Ghost.NAMESPACE, localPart = "getGhostRequest")
    @ResponsePayload
    fun getGhost(@RequestPayload request: GetGhostRequest): GetGhostResponse {
        val ghost = ghostService.findByType(request.type)
        return GetGhostResponse(ghost = ghost)
    }

    @PayloadRoot(namespace = Ghost.NAMESPACE, localPart = "listGhostsRequest")
    @ResponsePayload
    fun listGhosts(@RequestPayload request: ListGhostsRequest): ListGhostsResponse {
        val ghosts = ghostService.findAll()
        return ListGhostsResponse(ghosts = ghosts.toList())
    }
}