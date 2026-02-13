package org.khorum.oss.spekter.examples.testcommon

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.time.Duration

class SpektrContainer(
    imageName: String = "spektr:local",
    exposedPort: Int = 8080
) : GenericContainer<SpektrContainer>(DockerImageName.parse(imageName)) {

    init {
        withExposedPorts(exposedPort)
        waitingFor(
            Wait.forHttp("/actuator/health")
                .forPort(8080)
                .withStartupTimeout(Duration.ofSeconds(60))
        )
    }

    fun withEndpointJars(hostPath: String): SpektrContainer = apply {
        withCopyFileToContainer(
            MountableFile.forHostPath(hostPath),
            "/app/endpoint-jars"
        )
    }

    fun withRestEnabled(enabled: Boolean = true): SpektrContainer = apply {
        withEnv("SPEKTR_REST_ENABLED", enabled.toString())
    }

    fun withSoapEnabled(enabled: Boolean = true): SpektrContainer = apply {
        withEnv("SPEKTR_SOAP_ENABLED", enabled.toString())
    }

    val baseUrl: String
        get() = "http://$host:$firstMappedPort"
}