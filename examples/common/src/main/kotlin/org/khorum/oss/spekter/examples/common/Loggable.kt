package org.khorum.oss.spekter.examples.common

import mu.two.KLogger
import mu.two.KLogging

private val K_LOGGING = KLogging()

private val LOG_MAP = mutableMapOf<String, KLogger>()

interface Loggable {
    val log: KLogger
        get() {
            val name = this::class.simpleName ?: "DefaultLogger"
            return LOG_MAP.getOrPut(name) { K_LOGGING.logger(name) }
        }
}
