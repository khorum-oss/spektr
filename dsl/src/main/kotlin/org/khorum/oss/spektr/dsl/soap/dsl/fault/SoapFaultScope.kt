package org.khorum.oss.spektr.dsl.soap.dsl.fault

import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker
import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementBuilder

// ── Fault scope: what callers see ──────────────────────
// Common surface shared by both versions.
// Version-specific methods just no-op or throw on the wrong version,
// OR you split into two blocks — see the alternative below.

interface SoapFaultScope {
    fun detail(block: SoapElementBuilder.() -> Unit)

    // 1.1 methods
    fun faultCode(code: String)
    fun faultString(reason: String)
    fun faultActor(actor: String)

    // 1.2 methods
    fun code(value: String)
    @SoapDslMarker
    fun code(block: SoapFaultCode.() -> Unit)
    @SoapDslMarker
    fun reason(block: SoapFaultReason.() -> Unit)
    fun node(node: String)
    fun role(role: String)
}