package org.khorum.oss.spekter.examples.common

import java.util.UUID

data class HauntedHouse(
    val id: UUID,
    val address: Address,
    val ghosts: Map<Ghost, GhostReport>
)

data class CreateHauntedHouseRequest(
    val address: Address? = null,
    val ghosts: Map<Ghost, CreateGhostReportRequest>? = null
)