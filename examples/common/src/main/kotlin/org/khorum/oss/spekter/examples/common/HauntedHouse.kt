package org.khorum.oss.spekter.examples.common

import java.util.UUID

data class HauntedHouse(
    val id: UUID,
    val address: Address,
    val ghosts: Map<Ghost, GhostReport>
)

/** A unique type of ghost */
typealias GhostType = String

data class CreateHauntedHouseRequest(
    val address: Address? = null,
    val ghosts: List<GhostType>? = null
)