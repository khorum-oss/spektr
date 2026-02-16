package org.khorum.oss.spektr.examples.base

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue
import org.khorum.oss.spektr.dsl.rest.DynamicRequest
import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.rest.RestEndpointRegistry
import java.util.UUID

private val OBJECT_MAPPER: ObjectMapper = JsonMapper.builder()
    .addModule(KotlinModule.Builder().build())
    .build()

private fun ObjectMapper.readHouse(body: String?): House = readValue(body ?: throw IllegalStateException("No body"))

data class House(
    var id: UUID? = UUID.randomUUID(),
    var isHaunted: Boolean = true
)

data class PatchHouseRequest(
    var isHaunted: Boolean? = null
)

private val DynamicRequest.pathId: UUID
    get() = UUID.fromString(pathVariables["id"])

class HouseEndpoints : EndpointModule {
    private val collection: MutableMap<UUID, House> = mutableMapOf()

    init {
        val id1 = UUID.fromString("dbf40fb3-e1bd-4683-8a78-547f054e4d42")
        collection[id1] = House(id1)
        val id2 = UUID.fromString("7a99c0dc-64cf-4e0a-948c-1d2a6a191f30")
        collection[id2] = House(id2)
    }

    override fun RestEndpointRegistry.configure() {
        get("/api/houses/{id}") { request ->
            val id = request.pathId
            returnBody(collection[id])
        }

        get("/api/house") { request ->
            val isHaunted = request.queryParams["is-haunted"]?.firstOrNull()?.toBoolean()

            val houses = isHaunted
                ?.let { collection.filterValues { house -> house.isHaunted == it } }
                ?: collection

            returnBody(houses.values)
        }

        post("/api/house") { request ->
            val newHouse: House = OBJECT_MAPPER.readHouse(request.body)
            if (newHouse.id == null) {
                newHouse.id = UUID.randomUUID()
            }
            collection[newHouse.id!!] = newHouse
            returnBody(newHouse)
        }

        put("/api/house") { request ->
            val updatedHouse: House = OBJECT_MAPPER.readHouse(request.body)
            collection[updatedHouse.id!!] = updatedHouse
            returnBody(updatedHouse)
        }

        patch("/api/house/{id}") { request ->
            val id: UUID = request.pathId
            val body = requireNotNull(request.body) { "No body" }
            val updatedHouse: PatchHouseRequest = OBJECT_MAPPER.readValue(body)
            val isHaunted = requireNotNull(updatedHouse.isHaunted) { "No isHaunted in request" }
            collection[id]?.isHaunted = isHaunted
            returnBody(collection[id])
        }

        delete("/api/house/{id}") { request ->
            val id: UUID = request.pathId
            collection.remove(id)
            returnStatus(204)
        }
    }
}