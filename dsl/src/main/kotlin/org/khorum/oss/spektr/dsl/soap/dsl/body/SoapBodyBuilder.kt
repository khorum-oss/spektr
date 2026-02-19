package org.khorum.oss.spektr.dsl.soap.dsl.body

import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker
import org.khorum.oss.spektr.dsl.soap.dsl.content.SoapElementHolder
import org.khorum.oss.spektr.dsl.soap.dsl.SoapVersion
import org.khorum.oss.spektr.dsl.soap.dsl.fault.SoapFaultScope

class SoapBodyBuilder(private val version: SoapVersion) : SoapBodyContent, SoapElementHolder() {
    private var fault: SoapFaultBuilder? = null

    // Internal getter for serialization
    internal fun getFault(): SoapFaultBuilder? = fault

    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        fault = version.faultBuilder().apply(block)
    }
}