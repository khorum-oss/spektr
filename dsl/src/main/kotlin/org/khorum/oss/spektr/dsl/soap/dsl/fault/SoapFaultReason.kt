package org.khorum.oss.spektr.dsl.soap.dsl.fault

/**
 * Data class for SOAP 1.2 fault reasons.
 *
 * Contains the human-readable fault description and its language tag.
 *
 * Example:
 * ```kotlin
 * reason {
 *     text = "The requested resource was not found"
 *     lang = "en"
 * }
 * // Produces: <env:Text xml:lang="en">The requested resource was not found</env:Text>
 * ```
 */
class SoapFaultReason {
    /** The human-readable fault description. */
    var text: String? = null

    /** The language tag for the text (default: "en" if not specified). */
    var lang: String? = null
}
