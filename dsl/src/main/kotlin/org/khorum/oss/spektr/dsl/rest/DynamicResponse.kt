package org.khorum.oss.spektr.dsl.rest

data class DynamicResponse(
    val status: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null
) {
    class Builder {
        var status: Int = 200
        private val headers: MutableMap<String, String> = mutableMapOf()
        var body: Any? = null

        fun header(key: String, value: String) {
            headers[key] = value
        }

        fun headers(vararg pairs: Pair<String, String>) {
            headers.putAll(pairs)
        }

        fun options(scope: Option.() -> Unit) {
            scope(Option())
        }

        fun build(): DynamicResponse {
            return DynamicResponse(status, headers, body)
        }

        inner class Option {
            private var failure: Boolean = false

            fun badRequest(check: Boolean, errorBody: Any? = null) {
                if (!failure && check) {
                    failure = true
                    status = 400
                    body = errorBody
                }
            }

            fun notFound(check: Boolean, errorBody: Any? = null) {
                if (!failure && check) {
                    failure = true
                    status = 404
                    body = errorBody
                }
            }

            fun ok(successBody: Any? = null, check: Boolean = true) {
                if (!failure && check) {
                    status = 200
                    body = successBody
                }
            }
        }
    }
}