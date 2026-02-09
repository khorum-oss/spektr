package org.khorum.oss.spektr.hauntedhousetracker.service

import org.khorum.oss.spekter.examples.common.Address
import org.khorum.oss.spekter.examples.common.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.Ghost
import org.khorum.oss.spekter.examples.common.GhostReport
import org.khorum.oss.spekter.examples.common.GhostType
import org.khorum.oss.spekter.examples.common.HauntedHouse
import org.khorum.oss.spekter.examples.common.Loggable
import org.khorum.oss.spektr.hauntedhousetracker.client.GhostSoapClient
import org.khorum.oss.spektr.hauntedhousetracker.repo.HauntedHouseRepo
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HauntedHouseService(
    private val ghostSoapClient: GhostSoapClient,
    private val hauntedHouseRepo: HauntedHouseRepo
) : Loggable {
    suspend fun createHauntedHouse(request: CreateHauntedHouseRequest): HauntedHouse {
        log.info { "Creating haunted house: $request" }
        val proposedGhostTypes: List<GhostType> = request.ghosts ?: emptyList()

        val existingGhosts: List<Ghost> = proposedGhostTypes.mapNotNull {
            ghostSoapClient.getGhost(it)
        }

        val existingGhostTypes: List<GhostType> = existingGhosts.map { it.type }

        if (existingGhostTypes.isNotEmpty()) {
            log.info { "Existing ghosts: $existingGhostTypes" }
        }

        val newGhostTypes: List<GhostType> = proposedGhostTypes.filter {
            ghostType -> ghostType !in existingGhostTypes
        }

        if (newGhostTypes.isNotEmpty()) {
            log.info { "New ghosts: $newGhostTypes" }
        }

        val newGhosts: List<Ghost> = newGhostTypes
            .map { CreateGhostRequest(type = it) }
            .map { ghostSoapClient.createGhost(it) }

        if (newGhosts.isEmpty()) {
            log.info { "No new ghosts were created" }
        } else {
            log.info { "New ghosts were created: $newGhosts" }
        }

        val allGhosts: List<Ghost> = existingGhosts + newGhosts

        val reportsByGhost: Map<Ghost, GhostReport> = allGhosts.associateWith { GhostReport(1) }

        val address: Address = requireNotNull(request.address)

        val newHauntedHouse = HauntedHouse(
            id = UUID.randomUUID(),
            address = address,
            ghosts = reportsByGhost
        )

        hauntedHouseRepo.saveGhost(newHauntedHouse)

        log.info { "Created haunted house: $newHauntedHouse" }

        return newHauntedHouse
    }

    suspend fun getHauntedHouses(): List<HauntedHouse> {
        return hauntedHouseRepo.getHauntedHouses().toList()
    }

    suspend fun getHauntedHousesById(id: UUID): HauntedHouse? {
        return hauntedHouseRepo.getHauntedHouse(id)
    }
}