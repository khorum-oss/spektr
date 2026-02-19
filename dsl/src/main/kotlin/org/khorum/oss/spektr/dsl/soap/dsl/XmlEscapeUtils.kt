package org.khorum.oss.spektr.dsl.soap.dsl

/**
 * Escapes special XML characters in text content.
 *
 * Converts the following characters to their XML entity equivalents:
 * - `&` → `&amp;`
 * - `<` → `&lt;`
 * - `>` → `&gt;`
 *
 * @param text The text to escape.
 * @return The escaped text safe for use in XML content.
 */
internal fun escapeXml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

/**
 * Escapes special XML characters in attribute values.
 *
 * Includes all escapes from [escapeXml] plus:
 * - `"` → `&quot;`
 * - `'` → `&apos;`
 *
 * @param text The attribute value to escape.
 * @return The escaped text safe for use in XML attribute values.
 */
internal fun escapeXmlAttr(text: String): String = escapeXml(text)
    .replace("\"", "&quot;")
    .replace("'", "&apos;")
