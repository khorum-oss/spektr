package org.khorum.oss.spektr.dsl.soap

/**
 * Represents an incoming SOAP request.
 *
 * This data class encapsulates all the information from a SOAP HTTP request
 * that a [SoapHandler] needs to process and generate a response.
 *
 * @property headers HTTP headers from the request, where each header name maps to a list of values.
 * @property soapAction The SOAPAction header value identifying the operation to invoke.
 * @property body The raw SOAP XML body as a string, or null if no body was provided.
 */
data class SoapRequest(
    val headers: Map<String, List<String>>,
    val soapAction: String,
    val body: String?
)
