package org.khorum.oss.spektr.dsl.soap.dsl

import org.khorum.oss.spektr.dsl.soap.dsl.body.Soap11FaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.Soap12FaultBuilder
import org.khorum.oss.spektr.dsl.soap.dsl.body.SoapFaultBuilder

// ── Version drives fault construction ──────────────────

enum class SoapVersion {
    V1_1 {
        override fun faultBuilder() = Soap11FaultBuilder()
    },
    V1_2 {
        override fun faultBuilder() = Soap12FaultBuilder()
    };

    abstract fun faultBuilder(): SoapFaultBuilder
}