package org.khorum.oss.spektr

import org.khorum.oss.spektr.config.SpektrProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SpektrProperties::class)
class SpektrApplication

fun main(args: Array<String>) {
	runApplication<SpektrApplication>(*args)
}
