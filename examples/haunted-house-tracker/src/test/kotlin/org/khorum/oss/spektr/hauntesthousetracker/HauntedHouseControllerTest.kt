package org.khorum.oss.spektr.hauntesthousetracker

import org.junit.jupiter.api.Test
import org.khorum.oss.spekter.examples.common.domain.CreateHauntedHouseRequest
import org.khorum.oss.spekter.examples.common.domain.UsAddress
import org.khorum.oss.spekter.examples.testcommon.TestClient
import org.khorum.oss.spekter.examples.testcommon.WithSpektr
import org.khorum.oss.spekter.examples.testcommon.configureShared
import org.khorum.oss.spektr.hauntedhousetracker.HauntedHouseTrackerApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
@SpringBootTest(
    classes = [HauntedHouseTrackerApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WithSpektr(
    endpointJarsPath = "../docker/endpoint-jars",
    properties = ["ghost-book.base-url"]
)
class HauntedHouseControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient
) {
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
