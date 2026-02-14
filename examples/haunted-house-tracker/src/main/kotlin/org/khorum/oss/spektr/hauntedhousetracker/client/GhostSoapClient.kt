package org.khorum.oss.spektr.hauntedhousetracker.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.khorum.oss.spekter.examples.common.domain.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.domain.CreateGhostResponse
import org.khorum.oss.spekter.examples.common.domain.GetGhostRequest
import org.khorum.oss.spekter.examples.common.domain.GetGhostResponse
import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.khorum.oss.spekter.examples.common.domain.ListGhostsRequest
import org.khorum.oss.spekter.examples.common.domain.ListGhostsResponse
import org.springframework.stereotype.Service
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.soap.client.core.SoapActionCallback

@Service
class GhostSoapClient(
    private val webServiceTemplate: WebServiceTemplate
) {
    suspend fun createGhost(request: CreateGhostRequest): Ghost = withContext(Dispatchers.IO) {
        val response = webServiceTemplate.marshalSendAndReceive(
            request,
            SoapActionCallback("CreateGhost")
        ) as CreateGhostResponse
        response.ghost
    }

    suspend fun getGhost(type: String): Ghost? = withContext(Dispatchers.IO) {
        val response = webServiceTemplate.marshalSendAndReceive(
            GetGhostRequest(type = type),
            SoapActionCallback("GetGhost")
        ) as GetGhostResponse
        response.ghost
    }

    suspend fun listGhosts(): List<Ghost> = withContext(Dispatchers.IO) {
        val response = webServiceTemplate.marshalSendAndReceive(
            ListGhostsRequest(),
            SoapActionCallback("ListGhosts")
        ) as ListGhostsResponse
        response.ghosts
    }
}