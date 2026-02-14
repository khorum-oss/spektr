package org.khorum.oss.spektr.ghostbook.client

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.khorum.oss.spekter.examples.common.domain.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.domain.GhostType
import org.khorum.oss.spekter.examples.common.domain.HauntedHouse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Service
class HauntedHouseClient(
    private val hauntedHouseTrackerWebClient: WebClient
) {
    suspend fun createHauntedHouse(request: CreateHauntedHouseRequest): HauntedHouse {
        return hauntedHouseTrackerWebClient
            .post()
            .uri("/haunted-houses")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<HauntedHouse>()
            .awaitSingle()
    }

    suspend fun getHauntedHouses(type: GhostType): List<HauntedHouse> {
        return hauntedHouseTrackerWebClient
            .get()
            .uri("/haunted-houses") { builder -> builder.queryParam("type", type).build() }
            .retrieve()
            .bodyToMono<List<HauntedHouse>>()
            .awaitSingle()
    }
}