package org.khorum.oss.spektr.hauntesthousetracker

import org.junit.jupiter.api.Test
import org.khorum.oss.spekter.examples.common.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.UsAddress
import org.khorum.oss.spektr.hauntedhousetracker.HauntedHouseTrackerApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.time.Duration

@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [HauntedHouseTrackerApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class HauntedHouseControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
    companion object {
        val spektrContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("spektr:local"))
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                MountableFile.forHostPath("../docker/endpoint-jars"),
                "/app/endpoint-jars"
            )
            .withEnv("SPEKTR_REST_ENABLED", "true")
            .withEnv("SPEKTR_SOAP_ENABLED", "true")
            .waitingFor(
                Wait.forHttp("/actuator/health")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(60))
            )
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("ghost-book.base-url") {
                "http://${spektrContainer.host}:${spektrContainer.firstMappedPort}"
            }
        }
    }

    private val uri = "/haunted-houses"

    val testClient: TestClient by lazy {
        configureShared(webTestClient, uri)
    }

    private val address = UsAddress(
        streetLine1 = "123 Elm Street",
        city = "Minneapolis",
        state = "Minnesota",
        postalCode = "55408"
    )

    @Test
    fun `create haunted house with no ghost types should not call ghost book`() {
        //given
        val request = CreateHauntedHouseRequest(address = address)

        //when
        testClient.post {
            withBody(request)
            //then
            expect {
                hasOkStatus()
                "$.address.streetLine1".jsonPathValueEquals(address.streetLine1)
                "$.address.city".jsonPathValueEquals(address.city)
                "$.address.state".jsonPathValueEquals(address.state)
                "$.address.postalCode".jsonPathValueEquals(address.postalCode)
                "$.address.country".jsonPathValueEquals("US")
                "$.ghosts".jsonPath { isEmpty }
            }
        }
    }

    @Test
    fun `create haunted house with an existing ghost and a new ghost`() {
        //given
        val request = CreateHauntedHouseRequest(
            address = address,
            ghosts = listOf("Shade", "Obake")
        )

        //when
        testClient.post {
            withBody(request)
            //then
            expect {
                hasOkStatus()
                "$.address.streetLine1".jsonPathValueEquals(address.streetLine1)
                "$.address.city".jsonPathValueEquals(address.city)
                "$.address.state".jsonPathValueEquals(address.state)
                "$.address.postalCode".jsonPathValueEquals(address.postalCode)
                "$.address.country".jsonPathValueEquals("US")
                "$.ghosts".jsonPath { isNotEmpty }
            }
        }
    }
}
