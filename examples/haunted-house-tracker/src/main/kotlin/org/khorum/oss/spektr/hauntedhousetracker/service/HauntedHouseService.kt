package org.khorum.oss.spektr.hauntedhousetracker.service

import org.khorum.oss.spekter.examples.common.Address
import org.khorum.oss.spekter.examples.common.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.Ghost
import org.khorum.oss.spekter.examples.common.GhostReport
import org.khorum.oss.spekter.examples.common.GhostType
import org.khorum.oss.spekter.examples.common.HauntedHouse
import org.khorum.oss.spektr.hauntedhousetracker.client.GhostSoapClient
import org.khorum.oss.spektr.hauntedhousetracker.repo.HauntedHouseRepo
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HauntedHouseService(
    private val ghostSoapClient: GhostSoapClient,
    private val hauntedHouseRepo: HauntedHouseRepo
) {
    suspend fun createHauntedHouse(request: CreateHauntedHouseRequest): HauntedHouse {
        val proposedGhostTypes: List<GhostType> = request.ghosts ?: emptyList()

        val existingGhosts: List<Ghost> = proposedGhostTypes.mapNotNull {
            ghostSoapClient.getGhost(it)
        }

        val existingGhostTypes: List<GhostType> = existingGhosts.map { it.type }

        val newGhostTypes: List<GhostType> = proposedGhostTypes.filter {
            ghostType -> ghostType !in existingGhostTypes
        }

        val newGhosts: List<Ghost> = newGhostTypes
            .map { CreateGhostRequest(type = it) }
            .map { ghostSoapClient.createGhost(it) }

        val allGhosts: List<Ghost> = existingGhosts + newGhosts

        val reportsByGhost: Map<Ghost, GhostReport> = allGhosts.associateWith { GhostReport(1) }

        val address: Address = requireNotNull(request.address)

        val newHauntedHouse = HauntedHouse(
            id = UUID.randomUUID(),
            address = address,
            ghosts = reportsByGhost
        )

        hauntedHouseRepo.saveGhost(newHauntedHouse)

        return newHauntedHouse
    }

    suspend fun getHauntedHouses(): List<HauntedHouse> {
        return hauntedHouseRepo.getHauntedHouses().toList()
    }

    suspend fun getHauntedHousesById(id: UUID): HauntedHouse? {
        return hauntedHouseRepo.getHauntedHouse(id)
    }
}