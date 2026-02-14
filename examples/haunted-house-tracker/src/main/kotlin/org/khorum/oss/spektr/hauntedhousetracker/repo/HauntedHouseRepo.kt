package org.khorum.oss.spektr.hauntedhousetracker.repo

import org.khorum.oss.spekter.examples.common.domain.HauntedHouse
import org.khorum.oss.spekter.examples.common.Loggable
import org.springframework.stereotype.Component
import java.util.*

@Component
class HauntedHouseRepo : Loggable {
    private val hauntedHouseDb = mutableMapOf<UUID, HauntedHouse>()

    fun saveGhost(hauntedHouse: HauntedHouse) {
        log.debug("Saving haunted house $hauntedHouse")
        hauntedHouseDb[hauntedHouse.id] = hauntedHouse
        log.debug("Saved haunted house $hauntedHouse")
    }

    fun getHauntedHouse(id: UUID): HauntedHouse? {
        log.debug("Getting haunted house. id: $id")
        val hauntedHouse = hauntedHouseDb[id]
        log.debug("Got haunted house. id: $id")
        return hauntedHouse
    }

    fun getHauntedHouses(): Collection<HauntedHouse> {
        log.debug("Getting all haunted houses")
        val hauntedHouses = hauntedHouseDb.values.toList()
        log.debug("Got ${hauntedHouses.size} haunted houses")
        return hauntedHouses
    }

    fun deleteHauntedHouse(id: UUID) {
        log.debug("Deleting haunted house. id: $id")
        hauntedHouseDb.remove(id)
        log.debug("Deleted haunted house. id: $id")
    }

    fun size() = hauntedHouseDb.size
}