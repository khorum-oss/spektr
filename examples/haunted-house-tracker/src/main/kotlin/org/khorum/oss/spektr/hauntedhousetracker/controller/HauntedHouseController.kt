package org.khorum.oss.spektr.hauntedhousetracker.controller

import org.khorum.oss.spekter.examples.common.domain.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.domain.GhostType
import org.khorum.oss.spekter.examples.common.domain.HauntedHouse
import org.khorum.oss.spektr.hauntedhousetracker.service.HauntedHouseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/haunted-houses")
class HauntedHouseController(
    private val hauntedHouseService: HauntedHouseService
) {
    @PostMapping
    suspend fun createHauntedHouse(@RequestBody request: CreateHauntedHouseRequest): HauntedHouse {
        return hauntedHouseService.createHauntedHouse(request)
    }

    @GetMapping
    suspend fun getHauntedHouses(@RequestParam type: GhostType? = null): List<HauntedHouse> {
        return hauntedHouseService.getHauntedHouses(type)
    }

    @GetMapping("{id}")
    suspend fun getHauntedHouses(@PathVariable id: UUID): HauntedHouse? {
        return hauntedHouseService.getHauntedHousesById(id)
    }
}