package org.khorum.oss.spektr.dsl.rest

/**
 * Enumeration of supported HTTP methods for REST endpoint definitions.
 */
enum class HttpMethod {
    /** HTTP GET method for retrieving resources. */
    GET,
    /** HTTP POST method for creating resources. */
    POST,
    /** HTTP PUT method for replacing resources. */
    PUT,
    /** HTTP PATCH method for partial resource updates. */
    PATCH,
    /** HTTP DELETE method for removing resources. */
    DELETE,
    /** HTTP OPTIONS method for CORS preflight and capability discovery. */
    OPTIONS
}
