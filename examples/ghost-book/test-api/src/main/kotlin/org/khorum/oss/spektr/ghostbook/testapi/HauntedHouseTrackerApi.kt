package org.khorum.oss.spektr.ghostbook.testapi

import org.khorum.oss.spekter.examples.common.domain.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.khorum.oss.spekter.examples.common.domain.GhostReport
import org.khorum.oss.spekter.examples.common.domain.HauntedHouse
import org.khorum.oss.spekter.examples.common.domain.UsAddress
import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.rest.RestEndpointRegistry
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.util.UUID

private val OBJECT_MAPPER: ObjectMapper = JsonMapper.builder()
    .addModule(KotlinModule.Builder().build())
    .build()

class HauntedHouseTrackerApi : EndpointModule {
    private val houseId1 = UUID.fromString("01bd32f5-325f-41d3-8047-4812d197a183")
    private val houseId2 = UUID.fromString("fa5ea8b6-e545-4b79-9310-da92fc04365f")
    private val houseId3 = UUID.fromString("8c94deaa-e8fb-4285-a7b7-082408795cf9")

    private val store: MutableMap<UUID, HauntedHouse> = sequenceOf(
        HauntedHouse(
            id = houseId1,
            address = UsAddress(
                streetLine1 = "108 Ocean Ave",
                city = "Amityville",
                state = "New York",
                postalCode = "11701"
            ),
            ghostReports = mapOf()
        ),
        HauntedHouse(
            id = houseId2,
            address = UsAddress(
                streetLine1 = "525 S Winchester Blvd",
                city = "San Jose",
                state = "California",
                postalCode = "95128"
            ),
            ghostReports = mapOf()
        ),
        HauntedHouse(
            id = houseId3,
            address = UsAddress(
                streetLine1 = "333 Wonderview Ave",
                city = "Estes Park",
                state = "Colorado",
                postalCode = "80517"
            ),
            ghostReports = mapOf()
        )
    ).associateBy { it.id }.toMutableMap()

    override fun RestEndpointRegistry.configure() {
        get("/haunted-houses") {
            returnBody(store.values.toList())
        }

        get("/haunted-houses/{id}") { request ->
            val id: UUID? = request.pathVariables["id"]?.let { UUID.fromString(it) }
            val house = id?.let(store::get)

            returnResponse {
                options {
                    badRequest(id == null, "Missing id")
                    notFound(house == null, "House not found")
                    ok(house)
                }
            }
        }

        post("/haunted-houses") { request ->
            val request = request.body?.let { OBJECT_MAPPER.readValue(it, CreateHauntedHouseRequest::class.java) }

            val house = if (request?.address?.streetLine1 == "1677 Round Top Rd") {
                HauntedHouse(
                    id = UUID.randomUUID(),
                    address = request.address!!,
                    ghostReports = request.ghosts?.associate { type -> Ghost(type) to GhostReport(1) } ?: emptyMap()
                )
            } else {
                null
            }

            returnResponse {
                options {
                    badRequest(request == null, "Missing request")
                    badRequest(house == null, "Failed to create house")
                    ok(house)
                }
            }
        }
    }
}