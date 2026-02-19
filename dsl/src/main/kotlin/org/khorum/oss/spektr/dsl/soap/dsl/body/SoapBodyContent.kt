package org.khorum.oss.spektr.dsl.soap.dsl.body

/**
 * Marker interface for content that can appear in a SOAP body.
 *
 * This sealed interface allows the body to contain either normal elements
 * ([SoapBodyBuilder]) or a fault response ([SoapFaultBuilder]).
 */
sealed interface SoapBodyContent
