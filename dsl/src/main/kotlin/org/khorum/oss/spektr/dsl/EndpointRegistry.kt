package org.khorum.oss.spektr.dsl

class EndpointRegistry {
    private val endpointList = mutableListOf<EndpointDefinition>()
    val endpoints: List<EndpointDefinition> get() = endpointList

    fun get(path: String, handler: DynamicHandler) = register(HttpMethod.GET, path, handler)
    fun post(path: String, handler: DynamicHandler) = register(HttpMethod.POST, path, handler)
    fun put(path: String, handler: DynamicHandler) = register(HttpMethod.PUT, path, handler)
    fun patch(path: String, handler: DynamicHandler) = register(HttpMethod.PATCH, path, handler)
    fun delete(path: String, handler: DynamicHandler) = register(HttpMethod.DELETE, path, handler)
    fun options(path: String, handler: DynamicHandler) = register(HttpMethod.OPTIONS, path, handler)

    fun returnBody(body: Any?): DynamicResponse = DynamicResponse(body = body)

    fun returnStatus(status: Int): DynamicResponse = DynamicResponse(status = status)

    private fun register(method: HttpMethod, path: String, handler: DynamicHandler) {
        endpointList.add(EndpointDefinition(method, path, handler))
    }

    // Error scenario helpers
    fun errorOn(method: HttpMethod,
                path: String,
                status: Int,
                body: Any? = null,
                condition: (DynamicRequest) -> Boolean = { true }) {
        register(method, path) { request ->
            if (condition(request)) {
                DynamicResponse(status = status, body = body)
            } else {
                DynamicResponse(status = 200, body = mapOf("status" to "ok"))
            }
        }
    }
}

fun endpoints(block: EndpointRegistry.() -> Unit): EndpointRegistry {
    return EndpointRegistry().apply(block)
}