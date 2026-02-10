package org.khorum.oss.spekter.ghostbook.repo

import org.khorum.oss.spekter.examples.common.Ghost
import org.khorum.oss.spekter.examples.common.Loggable
import org.springframework.stereotype.Component

@Component
class GhostRepo : Loggable {
    private val ghostDb = mutableMapOf<String, Ghost>()

    fun saveGhost(ghost: Ghost) {
        log.debug("Saving ghost: $ghost")
        ghostDb[ghost.type] = ghost
        log.debug("Saved ghost: $ghost")
    }

    fun getGhost(type: String): Ghost? {
        log.debug("Getting ghost type: $type")
        val ghost = ghostDb[type]
        log.debug("Got ghost type: $type")
        return ghost
    }

    fun getGhosts(): Collection<Ghost> {
        log.debug("Getting all ghosts")
        val ghosts = ghostDb.values.toList()
        log.debug("Got ${ghosts.size} ghosts")
        return ghosts
    }

    fun deleteGhost(origin: String) {
        log.debug("Deleting ghost from $origin")
        ghostDb.remove(origin)
        log.debug("Deleted ghost from $origin")
    }

    fun size() = ghostDb.size
}