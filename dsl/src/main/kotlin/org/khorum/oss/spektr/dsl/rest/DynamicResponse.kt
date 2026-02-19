package org.khorum.oss.spektr.dsl.rest

/**
 * Represents an HTTP response from a dynamic endpoint handler.
 *
 * This data class contains all the information needed to construct an HTTP response,
 * including status code, headers, and body content.
 *
 * @property status The HTTP status code (default: 200 OK).
 * @property headers Response headers as key-value pairs.
 * @property body The response body, which can be any type and will be serialized appropriately.
 */
data class DynamicResponse(
    val status: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null
) {
    /**
     * Builder class for constructing [DynamicResponse] instances with a fluent API.
     *
     * Provides convenient methods for setting response properties and handling
     * common response scenarios like errors.
     *
     * Example:
     * ```kotlin
     * val response = DynamicResponse.Builder().apply {
     *     status = 201
     *     header("Location", "/api/users/123")
     *     body = mapOf("id" to "123", "name" to "John")
     * }.build()
     * ```
     */
    class Builder {
        /** The HTTP status code for the response. */
        var status: Int = 200
        private val headers: MutableMap<String, String> = mutableMapOf()
        /** The response body content. */
        var body: Any? = null

        /**
         * Adds a single header to the response.
         *
         * @param key The header name.
         * @param value The header value.
         */
        fun header(key: String, value: String) {
            headers[key] = value
        }

        /**
         * Adds multiple headers to the response.
         *
         * @param pairs Vararg of key-value pairs representing headers.
         */
        fun headers(vararg pairs: Pair<String, String>) {
            headers.putAll(pairs)
        }

        /**
         * Applies conditional response options using the [Option] DSL.
         *
         * This allows for declarative handling of different response scenarios.
         *
         * @param scope Lambda with [Option] receiver for defining response conditions.
         */
        fun options(scope: Option.() -> Unit) {
            scope(Option())
        }

        /**
         * Builds the final [DynamicResponse] instance.
         *
         * @return The constructed response with all configured properties.
         */
        fun build(): DynamicResponse {
            return DynamicResponse(status, headers, body)
        }

        /**
         * Inner class providing conditional response handling options.
         *
         * Options are evaluated in order, and once a condition matches,
         * subsequent options are ignored (fail-fast behavior).
         */
        inner class Option {
            private var failure: Boolean = false

            /**
             * Sets a 400 Bad Request response if the condition is true.
             *
             * @param check The condition to evaluate.
             * @param errorBody Optional error body to include in the response.
             */
            fun badRequest(check: Boolean, errorBody: Any? = null) {
                if (!failure && check) {
                    failure = true
                    status = 400
                    body = errorBody
                }
            }

            /**
             * Sets a 404 Not Found response if the condition is true.
             *
             * @param check The condition to evaluate.
             * @param errorBody Optional error body to include in the response.
             */
            fun notFound(check: Boolean, errorBody: Any? = null) {
                if (!failure && check) {
                    failure = true
                    status = 404
                    body = errorBody
                }
            }

            /**
             * Sets a 200 OK response with the given body if no failure has occurred.
             *
             * @param successBody The body to include in the successful response.
             * @param check Additional condition that must be true (default: true).
             */
            fun ok(successBody: Any? = null, check: Boolean = true) {
                if (!failure && check) {
                    status = 200
                    body = successBody
                }
            }
        }
    }
}
