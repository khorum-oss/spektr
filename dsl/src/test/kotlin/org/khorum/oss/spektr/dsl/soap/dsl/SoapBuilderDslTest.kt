package org.khorum.oss.spektr.dsl.soap.dsl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.dsl.soap.SoapXml
import java.time.LocalDate
import kotlin.test.assertEquals

class SoapBuilderDslTest {
    val mainTag = "soapenv"
    val nsTag = "ns"

    /** Loads a resource file from the test resources directory. */
    private fun loadResourceXml(path: String): SoapXml {
        return this::class.java.classLoader
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?.let(::SoapXml)
            ?: throw IllegalArgumentException("Resource not found: $path")
    }

    /** Normalizes XML for comparison by removing declarations, comments, and collapsing whitespace. */
    private fun SoapXml.normalize(): SoapXml {
        return content
            .replace(Regex("<\\?xml[^?]*\\?>"), "") // Remove XML declaration
            .replace(Regex("<!--[\\s\\S]*?-->"), "") // Remove XML comments
            .replace(Regex("\\s+"), " ") // Collapse all whitespace to single spaces
            .replace(Regex("> <"), "><") // Remove space between tags
            .replace(Regex("^\\s+|\\s+$"), "") // Trim leading/trailing
            .trim()
            .let(::SoapXml)
    }

    private fun expectXmlEquals(
        builder: SoapEnvelopeBuilder,
        expectedFileLocation: String
    ) {
        val generatedXml = SoapXml(builder.toPrettyString()).normalize().content
        val expectedXml = loadResourceXml(expectedFileLocation).normalize().content
        assertEquals(
            expectedXml,
            generatedXml,
            """
                    ACTUAL: $generatedXml
                    EXPECT: $expectedXml

                """.trimIndent()
        )
    }

    @Nested
    inner class Soap11 {
        @Test
        fun `soap 1 1 server fault`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_1
                envelopePrefix = mainTag

                namespaces {
                    ns("xmlns:$nsTag", "http://org.khorum-oss.com/ghost-book")
                }

                fault {
                    faultCode("$mainTag:Server")
                    faultString("Ghost not found in registry")
                    faultActor("http://org.khorum-oss.com/ghost-book/lookup")
                    detail {
                        element(namespace = nsTag, name = "errorCode") { content = "GHOST_404" }
                        element(namespace = nsTag, name = "retryable") { content = false }
                        list(namespace = nsTag, name = "searchedRegistries") {
                            element(namespace = nsTag, name = "registry") { content = "primary" }
                            element(namespace = nsTag, name = "registry") { content = "archive" }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.1/soap-1.1-server-fault.xml")
        }

        @Test
        fun `soap 1 1 client fault`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_1
                envelopePrefix = mainTag

                namespaces {
                    ns("xmlns:$nsTag", "http://org.khorum-oss.com/ghost-book")
                }

                fault {
                    faultCode("$mainTag:Client")
                    faultString("Invalid request: missing required field 'type'")
                    detail {
                        list(namespace = nsTag, name = "validationErrors") {
                            element(namespace = nsTag, name = "field") {
                                attributes(
                                    "name" to "type",
                                    "reason" to "required"
                                )
                            }
                            element(namespace = nsTag, name = "field") {
                                attributes(
                                    "name" to "name",
                                    "reason" to "required"
                                )
                            }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.1/soap-1.1-client-fault.xml")
        }

        @Test
        fun `soap 1 1 minimum happy path`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_1
                envelopePrefix = "soap"

                namespaces {
                    ns("xmlns:$nsTag", "http://org.khorum-oss.com/ghost-book")
                }

                body {
                    element(nsTag, "deleteGhostResponse") {
                        element("success") {
                            content = true
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.1/soap-1.1-min-response.xml")
        }

        @Test
        fun `soap 1 1 full happy path`() {
            val commonTag = "common"

            val builder = soapEnvelope {
                version = SoapVersion.V1_1
                envelopePrefix = mainTag

                namespaces {
                    ns("xmlns:$nsTag", "http://org.khorum-oss.com/ghost-book")
                    ns("xmlns:$commonTag" to "http://org.khorum-oss.com/common")
                    ns("xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance")
                }

                header {
                    element(commonTag, "transactionId") { content = "txn-9f8a-4b21" }
                    element(commonTag, "routing") {
                        attribute(mainTag, "mustUnderstand", "1")
                        attribute(commonTag, "priority", "high")
                        content = "us-east-1"
                    }
                    element(commonTag, "sessionToken") { content = "eyJhbGciOiJSUzI1NiJ9.ghost-session" }
                }

                body {
                    element(nsTag, "createGhostResponse") {
                        element("ghost") {
                            element("type") { content = "Poltergeist" }
                            element("name") { content = "The Grey Lady" }
                            element("origin") { content = "Victorian Manor" }
                            element("hauntingScore") { content = 87.5 }
                            element("ability") {
                                attribute("level", 3)
                                content = "Telekinesis"
                            }
                            element("ability") {
                                attribute("level", 7)
                                content = "Invisibility"
                            }
                            element("ability") {
                                attribute("level", 1)
                                content = "Cold Spots"
                            }
                            element("lastSighting") {
                                element("location") { content = "East Wing Corridor" }
                                element("timestamp") { content = "2025-01-15T03:42:00Z" }
                                list("witnesses") {
                                    element("witness") {
                                        element("name") { content = "Dr. Elara Voss" }
                                        optional("credibility") { content = "high" }
                                    }
                                    element("witness") {
                                        element("name") { content = "Marcus Chen" }
                                        optional("credibility") { }
                                    }
                                }
                            }
                            element("description") {
                                cdata = "Appeared at <midnight> & vanished \"instantly\" — left no trace"
                            }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.1/soap-1.1-full-happy-path.xml")
        }

        @Test
        fun `soap 1 1 nillable`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_1
                envelopePrefix = "SOAP-ENV"

                namespaces {
                    ns("xmlns:$nsTag", "http://org.khorum-oss.com/ghost-book")
                    ns("xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance")
                }

                body {
                    element(nsTag, "getGhostResponse") {
                        element("ghost") {
                            element("type") { content = "Apparition" }
                            element("name") { content = "The Whisperer" }
                            nillable("hauntingScore") {}
                            element("lastSighting") {
                                element("location") { content = "Basement" }
                                element("timestamp") { content = "2025-03-22T23:01:00Z" }
                                list("witnesses") {

                                }
                            }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.1/soap-1.1-custom-prefix-nillable.xml")
        }
    }

    @Nested
    inner class Soap12 {
        @Test
        fun `soap 1 2 minimal fault`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_2
                envelopePrefix = "env"

                body {
                    fault {
                        code("env:Receiver")

                        reason {
                            lang = "en"
                            text = "Internal ghost registry unavailable"
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.2/soap-1.2-minimal-fault.xml")
        }

        @Test
        fun `soap 1 2 fault with sub codes`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_2
                envelopePrefix = "env"

                namespaces {
                    ns("xmlns:ns" to "http://org.khorum-oss.com/ghost-book")
                }

                body {
                    fault {
                        code {
                            value("env:Sender")
                            subcode("ns:InvalidGhostType")
                        }

                        reason {
                            text = "The ghost type 'friendly' is not recognized"
                        }

                        node("http://org.khorum-oss.com/ghost-book/validation")
                        role("http://org.khorum-oss.com/ghost-book/endpoint")

                        detail {
                            list("ns:validTypes") {
                                element("ns:type") { content = "Poltergeist" }
                                element("ns:type") { content = "Apparition" }
                                element("ns:type") { content = "Shadow" }
                                element("ns:type") { content = "Banshee" }
                                element("ns:type") { content = "Wraith" }
                            }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.2/soap-1.2-fault-with-nested-sub-codes.xml")
        }

        @Test
        fun `soap 1 2 full happy path`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_2
                envelopePrefix = "env"

                namespaces {
                    ns("xmlns:ns" to "http://org.khorum-oss.com/ghost-book")
                    ns("xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance")
                }

                header {
                    element(namespace = nsTag, name = "correlationId") {
                        attribute("env:mustUnderstand", true)
                        content = "corr-88af-11ee"
                    }
                }

                body {
                    element("ns:listGhostsResponse") {
                        list("ghosts") {
                            element("ghost") {
                                element("type") { content = "Shadow" }
                                element("name") { content = "Umbra" }
                                element("hauntingScore") { content = 42.0 }
                                element("ability") {
                                    attribute("level", 5)
                                    content = "Shadow Manipulation"
                                }
                            }

                            element("ghost") {
                                element("type") { content = "Banshee" }
                                element("name") { content = "The Wailing One" }
                                element("origin") { content = "Celtic Ruins" }
                                nillable("hauntingScore") {}
                                element("ability") {
                                    attribute("level", 9)
                                    content = "Death Wail"
                                }
                                element("ability") {
                                    attribute("level", 4)
                                    content = "Precognition"
                                }
                            }
                        }
                        element("totalCount") { content = 2 }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.2/soap-1.2-full-happy-path.xml")
        }

        @Test
        fun `soap 1 2 deeply nested`() {
            val builder = soapEnvelope {
                version = SoapVersion.V1_2
                envelopePrefix = "soapenv"

                namespaces {
                    ns("xmlns:ns" to "http://org.khorum-oss.com/ghost-book")
                    ns("xmlns:geo" to "http://org.khorum-oss.com/geo")
                }

                body {
                    element("ns:getHauntedLocationsResponse") {
                        list("locations") {
                            element("location") {
                                element("name") { content = "Winchester Mystery House" }
                                element("geo:coordinates") {
                                    element("geo:latitude") { content = 37.3184 }
                                    element("geo:longitude") { content = -121.9511 }
                                    element("geo:elevation") {
                                        attribute("unit", "meters")
                                        content = 58.0
                                    }
                                }
                                list("ghosts") {
                                    element("ghost") {
                                        element("type") { content = "apparition" }
                                        element("name") { content = "Sarah Winchester" }
                                        list("sightings") {
                                            element("sighting") {
                                                attribute("floor", "2")
                                                attribute("room", "Séance Room")
                                                element("date") { content = LocalDate.of(2024, 10, 31) }
                                                element("description") {
                                                    cdata =
                                                        """Translucent figure seen near the fireplace — "cold draft" reported by 3 visitors"""
                                                }
                                            }
                                            element("sighting") {
                                                attribute("floor", "3")
                                                attribute("room", "Door to Nowhere")
                                                element("date") { content = LocalDate.of(2025, 1, 1) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            expectXmlEquals(builder, "soap-1.2/soap-1.2-deeply-nested.xml")
        }
    }
}