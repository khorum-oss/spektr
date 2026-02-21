package org.khorum.oss.spektr.dsl.rest

/**
 * Represents an incoming HTTP request for dynamic endpoint handling.
 *
 * This data class encapsulates all the request information needed by a [DynamicHandler]
 * to process the request and generate an appropriate response.
 *
 * @property headers HTTP headers from the request, where each header name maps to a list of values.
 * @property pathVariables Path template variables extracted from the URL (e.g., `/users/{id}` -> `{"id": "123"}`).
 * @property queryParams Query string parameters, where each parameter name maps to a list of values.
 * @property body The raw request body as a string, or null if no body was provided.
 */
data class DynamicRequest(
    val headers: Map<String, List<String>>,
    val pathVariables: Map<String, String>,
    val queryParams: Map<String, List<String>>,
    val body: String?
)
