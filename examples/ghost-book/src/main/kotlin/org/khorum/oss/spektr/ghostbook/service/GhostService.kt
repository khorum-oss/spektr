package org.khorum.oss.spektr.ghostbook.service

import org.khorum.oss.spekter.examples.common.domain.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.domain.CreateGhostResponse
import org.khorum.oss.spekter.examples.common.domain.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.domain.GetGhostRequest
import org.khorum.oss.spekter.examples.common.domain.GetGhostResponse
import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.khorum.oss.spektr.ghostbook.client.HauntedHouseClient
import org.khorum.oss.spektr.ghostbook.repo.GhostRepo
import org.springframework.stereotype.Service

@Service
class GhostService(
    private val ghostRepo: GhostRepo,
    private val hauntedHouseClient: HauntedHouseClient
) {

    suspend fun create(request: CreateGhostRequest): CreateGhostResponse {
        val ghost = Ghost(
            type = requireNotNull(request.type) { "Ghost type is required" },
            origin = request.origin
        )

        val exists = ghostRepo.getGhost(ghost.type) != null

        if (exists) {
            throw IllegalArgumentException("Ghost already exists")
        }

        val houses = request.addresses
            ?.map { CreateHauntedHouseRequest(address = it) }
            ?.map { hauntedHouseClient.createHauntedHouse(it) }

        ghostRepo.saveGhost(ghost)

        return CreateGhostResponse(ghost, houses)
    }

    suspend fun findByType(request: GetGhostRequest): GetGhostResponse? {
        val ghost = ghostRepo.getGhost(request.type) ?: return null

        val houses = hauntedHouseClient.getHauntedHouses(request.type)

        return GetGhostResponse(ghost, houses)
    }

    suspend fun findAll(withHouses: Boolean): Collection<Ghost> {
        val ghosts = ghostRepo.getGhosts()

        if (!withHouses) return ghosts

        return ghosts.onEach {
            val houses = hauntedHouseClient.getHauntedHouses(it.type)
            it.houses = houses
        }
    }
}