package org.khorum.oss.spektr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpektrApplication

fun main(args: Array<String>) {
	runApplication<SpektrApplication>(*args)
}
