package org.khorum.oss.spektr.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spektr")
data class SpektrProperties(
    val rest: ProtocolProperties = ProtocolProperties(),
    val soap: ProtocolProperties = ProtocolProperties()
) {
    data class ProtocolProperties(
        val enabled: Boolean = true
    )
}
