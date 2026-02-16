package org.khorum.oss.spektr.dsl.rest

fun interface DynamicHandler {
    fun handle(request: DynamicRequest): DynamicResponse
}