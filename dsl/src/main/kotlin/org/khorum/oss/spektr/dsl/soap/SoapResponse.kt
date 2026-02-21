package org.khorum.oss.spektr.dsl.soap

/**
 * Represents a SOAP response to be returned to the client.
 *
 * This data class contains all the information needed to construct an HTTP response
 * for a SOAP request, including status code, headers, and the SOAP XML body.
 *
 * @property status The HTTP status code (default: 200 OK).
 * @property headers Response headers as key-value pairs.
 * @property body The SOAP XML response body as a string, or null for an empty response.
 */
data class SoapResponse(
    val status: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)
