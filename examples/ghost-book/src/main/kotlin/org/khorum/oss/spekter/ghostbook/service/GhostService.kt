package org.khorum.oss.spekter.ghostbook.service

import org.khorum.oss.spekter.examples.common.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.GetGhostRequest
import org.khorum.oss.spekter.examples.common.Ghost
import org.khorum.oss.spekter.ghostbook.repo.GhostRepo
import org.springframework.stereotype.Service

@Service
class GhostService(
    private val ghostRepo: GhostRepo
) {

    fun create(request: CreateGhostRequest): Ghost {
        val ghost = Ghost(
            type = requireNotNull(request.type),
            origin = requireNotNull(request.origin)
        )

        val exists = ghostRepo.getGhost(ghost.type) != null

        if (exists) {
            throw IllegalArgumentException("Ghost already exists")
        }

        ghostRepo.saveGhost(ghost)

        return ghost
    }

    fun findByType(type: String): Ghost? {
        return ghostRepo.getGhost(type)
    }

    fun findAll(): Collection<Ghost> {
        return ghostRepo.getGhosts()
    }
}