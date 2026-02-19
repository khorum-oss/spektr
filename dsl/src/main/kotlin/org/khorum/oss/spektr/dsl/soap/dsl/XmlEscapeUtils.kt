package org.khorum.oss.spektr.dsl.soap.dsl

// ── XML escape utilities ──────────────────────────────

internal fun escapeXml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

internal fun escapeXmlAttr(text: String): String = escapeXml(text)
    .replace("\"", "&quot;")
    .replace("'", "&apos;")