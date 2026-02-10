package org.khorum.oss.spektr.hauntedhousetracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HauntedHouseTrackerApplication

fun main(args: Array<String>) {
	runApplication<HauntedHouseTrackerApplication>(*args)
}
