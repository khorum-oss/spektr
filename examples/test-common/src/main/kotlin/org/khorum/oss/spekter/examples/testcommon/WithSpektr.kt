package org.khorum.oss.spekter.examples.testcommon

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(SpektrExtension::class)
annotation class WithSpektr(
    val image: String = "spektr:local",
    val endpointJarsPath: String = "",
    val restEnabled: Boolean = true,
    val soapEnabled: Boolean = true,
    val properties: Array<String> = []
)
