package org.khorum.oss.spekter.examples.common

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.SchemaOutputResolver
import org.khorum.oss.spekter.examples.common.domain.CreateGhostRequest
import org.khorum.oss.spekter.examples.common.domain.CreateGhostResponse
import org.khorum.oss.spekter.examples.common.domain.GetGhostRequest
import org.khorum.oss.spekter.examples.common.domain.GetGhostResponse
import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.khorum.oss.spekter.examples.common.domain.ListGhostsRequest
import org.khorum.oss.spekter.examples.common.domain.ListGhostsResponse
import java.io.File
import java.io.StringWriter
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult

/**
 * Standalone XSD generator that can be invoked from Gradle.
 * Generates the XSD schema from JAXB-annotated Ghost classes and writes to a file.
 * Merges multiple schemas (from different namespaces) into a single XSD.
 *
 * Usage: java -cp <classpath> org.khorum.oss.spekter.examples.common.XsdGeneratorKt <output-file>
 */
fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Usage: XsdGenerator <output-file-path>" }

    val outputFile = File(args[0])
    outputFile.parentFile?.mkdirs()

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

    // Get both schemas - main namespace and default namespace with type definitions
    val mainSchema = schemas[Ghost.NAMESPACE]?.toString()
        ?: throw IllegalStateException("No schema was generated for namespace ${Ghost.NAMESPACE}")

    val typeSchema = schemas[""]?.toString()

    // Merge schemas: extract type definitions from default namespace schema and inline them
    val mergedSchema = if (typeSchema != null) {
        mergeSchemas(mainSchema, typeSchema)
    } else {
        mainSchema
    }

    outputFile.writeText(mergedSchema)

    println("Generated XSD schema: ${outputFile.absolutePath}")
}

/**
 * Merges the main schema with type definitions from the default namespace schema.
 * Removes the xs:import and inlines the complexType definitions.
 */
private fun mergeSchemas(mainSchema: String, typeSchema: String): String {
    // Extract complexType definitions from the type schema
    val typeDefRegex = Regex("""<xs:complexType[^>]*>.*?</xs:complexType>""", RegexOption.DOT_MATCHES_ALL)
    val typeDefinitions = typeDefRegex.findAll(typeSchema).map { it.value }.toList()

    // Remove the xs:import line from main schema
    var merged = mainSchema.replace(Regex("""<xs:import[^>]*/>"""), "")

    // Find the closing </xs:schema> tag and insert type definitions before it
    val closingTag = "</xs:schema>"
    val insertPosition = merged.lastIndexOf(closingTag)

    if (insertPosition > 0 && typeDefinitions.isNotEmpty()) {
        val typeDefsString = typeDefinitions.joinToString("\n\n  ")
        merged = merged.substring(0, insertPosition) +
                 "\n  " + typeDefsString + "\n\n" +
                 merged.substring(insertPosition)
    }

    return merged
}
