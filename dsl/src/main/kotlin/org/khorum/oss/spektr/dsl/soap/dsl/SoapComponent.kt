package org.khorum.oss.spektr.dsl.soap.dsl

interface SoapComponent {
    fun toPrettyString(indent: String = "  "): String
}