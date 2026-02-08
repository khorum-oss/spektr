package org.khorum.oss.spekter.examples.common

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.SchemaOutputResolver
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import java.io.StringWriter
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

/**
 * Generates XSD schema from JAXB-annotated Ghost classes.
 * The schema is generated in-memory and can be retrieved as a Spring Resource.
 */
abstract class GhostXsdGenerator {

    private val schemaBytes: ByteArray by lazy { generateSchema() }

    /**
     * Returns the generated XSD schema as a Spring Resource.
     * This can be used directly with Spring WS XsdSchema.
     */
    fun getSchemaResource(): Resource {
        return ByteArrayResource(schemaBytes, "Generated ghosts.xsd from JAXB classes")
    }

    private fun generateSchema(): ByteArray {
        // Map to store schemas by namespace - we need to capture the main schema
        val schemas = mutableMapOf<String, StringWriter>()

        val resolver = object : SchemaOutputResolver() {
            override fun createOutput(namespace: String, suggestedFileName: String): Result {
                val writer = StringWriter()
                schemas[namespace] = writer
                return StreamResult(writer).apply {
                    systemId = suggestedFileName
                }
            }
        }

        val context = JAXBContext.newInstance(
            CreateGhostRequest::class.java,
            CreateGhostResponse::class.java,
            GetGhostRequest::class.java,
            GetGhostResponse::class.java,
            ListGhostsRequest::class.java,
            ListGhostsResponse::class.java
        )

        context.generateSchema(resolver)

        // Get the schema for the Ghost namespace (the main one we care about)
        val mainSchema = schemas[Ghost.NAMESPACE]
            ?: schemas.values.firstOrNull()
            ?: throw IllegalStateException("No schema was generated")

        return mainSchema.toString().toByteArray(Charsets.UTF_8)
    }
}