package org.khorum.oss.spektr.dsl.soap

/**
 * Defines a single SOAP endpoint operation with its path, SOAP action, and handler.
 *
 * This data class represents the complete definition of a SOAP operation that can be
 * registered and routed by the application's SOAP endpoint dispatcher.
 *
 * @property path The URL path where this SOAP endpoint is exposed.
 * @property soapAction The SOAPAction header value that identifies this operation.
 * @property handler The function that processes requests to this operation.
 */
data class SoapEndpointDefinition(
    val path: String,
    val soapAction: String,
    val handler: SoapHandler
)
