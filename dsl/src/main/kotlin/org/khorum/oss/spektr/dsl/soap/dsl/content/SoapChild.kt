package org.khorum.oss.spektr.dsl.soap.dsl.content

/**
 * Marker interface for child nodes that can be serialized within a SOAP element.
 *
 * This sealed interface allows type-safe handling of different child types
 * ([SoapElementBuilder] and [SoapListBuilder]) while maintaining insertion order
 * during serialization.
 */
sealed interface SoapChild
