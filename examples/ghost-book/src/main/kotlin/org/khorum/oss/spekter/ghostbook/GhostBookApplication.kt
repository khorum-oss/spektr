package org.khorum.oss.spekter.ghostbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GhostBookApplication

fun main(args: Array<String>) {
	runApplication<GhostBookApplication>(*args)
}
