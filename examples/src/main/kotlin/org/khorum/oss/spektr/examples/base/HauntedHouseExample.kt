package org.khorum.oss.spektr.examples.base

import org.khorum.oss.spektr.dsl.DynamicResponse
import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.EndpointRegistry

class HouseEndpoints : EndpointModule {
    override fun EndpointRegistry.configure() {
        get("/api/house/{id}") { request ->
            val id = request.pathVariables["id"]
            DynamicResponse(body = mapOf("id" to id, "name" to "Haunted House $id"))
        }
    }
}