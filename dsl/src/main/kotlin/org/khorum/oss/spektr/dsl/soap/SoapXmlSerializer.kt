package org.khorum.oss.spektr.dsl.soap

/**
 * Serializes a [SoapEnvelopeBuilder] to XML string.
 */
class SoapXmlSerializer(
    private val prettyPrint: Boolean = true,
    private val indent: String = "  "
) {
    private val soapNamespaces = mapOf(
        SoapVersion.V1_1 to "http://schemas.xmlsoap.org/soap/envelope/",
        SoapVersion.V1_2 to "http://www.w3.org/2003/05/soap-envelope"
    )

    /**
     * Converts a [SoapEnvelopeBuilder] to an XML string.
     */
    fun serialize(envelope: SoapEnvelopeBuilder): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        if (prettyPrint) sb.appendLine()

        serializeEnvelope(sb, envelope, 0)

        return sb.toString()
    }

    private fun serializeEnvelope(sb: StringBuilder, envelope: SoapEnvelopeBuilder, depth: Int) {
        val prefix = envelope.envelopePrefix
        val soapNs = envelope.schemasLocation ?: soapNamespaces[envelope.version]
            ?: throw IllegalArgumentException("Unknown SOAP version: ${envelope.version}")

        // Build envelope opening tag with namespaces
        sb.append(indentFor(depth))
        sb.append("<$prefix:Envelope")
        sb.append(" xmlns:$prefix=\"$soapNs\"")

        // Add custom namespaces
        envelope.getNamespaces()?.getNamespaces()?.forEach { (attr, uri) ->
            sb.append(" $attr=\"$uri\"")
        }
        sb.append(">")
        if (prettyPrint) sb.appendLine()

        // Serialize header if present
        envelope.getHeader()?.let { header ->
            serializeHeader(sb, header, prefix, depth + 1)
        }

        // Serialize body
        serializeBody(sb, envelope.getBody(), prefix, envelope.version, depth + 1)

        // Close envelope
        sb.append(indentFor(depth))
        sb.append("</$prefix:Envelope>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeHeader(sb: StringBuilder, header: SoapHeaderBuilder, prefix: String, depth: Int) {
        sb.append(indentFor(depth))
        sb.append("<$prefix:Header>")
        if (prettyPrint) sb.appendLine()

        serializeElementContent(sb, header, depth + 1)

        sb.append(indentFor(depth))
        sb.append("</$prefix:Header>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeBody(
        sb: StringBuilder,
        body: SoapBodyHolder?,
        prefix: String,
        version: SoapVersion,
        depth: Int
    ) {
        sb.append(indentFor(depth))
        sb.append("<$prefix:Body>")
        if (prettyPrint) sb.appendLine()

        when (body) {
            is SoapBodyBuilder -> {
                serializeElementContent(sb, body, depth + 1)
                // Check for fault inside body
                body.getFault()?.let { fault ->
                    serializeFault(sb, fault, prefix, version, depth + 1)
                }
            }
            is SoapFaultBuilder -> serializeFault(sb, body, prefix, version, depth + 1)
            null -> {} // Empty body
        }

        sb.append(indentFor(depth))
        sb.append("</$prefix:Body>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeFault(
        sb: StringBuilder,
        fault: SoapFaultBuilder,
        prefix: String,
        version: SoapVersion,
        depth: Int
    ) {
        sb.append(indentFor(depth))
        sb.append("<$prefix:Fault>")
        if (prettyPrint) sb.appendLine()

        when (fault) {
            is Soap11FaultBuilder -> serializeSoap11Fault(sb, fault, depth + 1)
            is Soap12FaultBuilder -> serializeSoap12Fault(sb, fault, prefix, depth + 1)
        }

        sb.append(indentFor(depth))
        sb.append("</$prefix:Fault>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeSoap11Fault(sb: StringBuilder, fault: Soap11FaultBuilder, depth: Int) {
        fault.getFaultCode()?.let { code ->
            sb.append(indentFor(depth))
            sb.append("<faultcode>")
            sb.append(escapeXml(code))
            sb.append("</faultcode>")
            if (prettyPrint) sb.appendLine()
        }

        fault.getFaultString()?.let { message ->
            sb.append(indentFor(depth))
            sb.append("<faultstring>")
            sb.append(escapeXml(message))
            sb.append("</faultstring>")
            if (prettyPrint) sb.appendLine()
        }

        fault.getFaultActor()?.let { actor ->
            sb.append(indentFor(depth))
            sb.append("<faultactor>")
            sb.append(escapeXml(actor))
            sb.append("</faultactor>")
            if (prettyPrint) sb.appendLine()
        }

        // Serialize detail if present
        fault.getDetail()?.let { detail ->
            sb.append(indentFor(depth + 1))
            sb.append("<detail>")
            if (prettyPrint) sb.appendLine()
            serializeElementContent(sb, detail, depth + 2)
            sb.append(indentFor(depth + 1))
            sb.append("</detail>")
            if (prettyPrint) sb.appendLine()
        }
    }

    private fun serializeSoap12Fault(sb: StringBuilder, fault: Soap12FaultBuilder, prefix: String, depth: Int) {
        fault.getCode()?.let { code ->
            sb.append(indentFor(depth))
            sb.append("<$prefix:Code>")
            if (prettyPrint) sb.appendLine()

            code.getValue()?.let { value ->
                sb.append(indentFor(depth + 1))
                sb.append("<$prefix:Value>")
                sb.append(escapeXml(value))
                sb.append("</$prefix:Value>")
                if (prettyPrint) sb.appendLine()
            }

            // Serialize subcodes recursively
            serializeSubcodes(sb, code.getSubcodes(), prefix, depth + 1)

            sb.append(indentFor(depth))
            sb.append("</$prefix:Code>")
            if (prettyPrint) sb.appendLine()
        }

        fault.getReason()?.let { reason ->
            sb.append(indentFor(depth))
            sb.append("<$prefix:Reason>")
            if (prettyPrint) sb.appendLine()
            sb.append(indentFor(depth + 1))
            sb.append("<$prefix:Text xml:lang=\"${reason.lang ?: "en"}\">")
            sb.append(escapeXml(reason.text ?: ""))
            sb.append("</$prefix:Text>")
            if (prettyPrint) sb.appendLine()
            sb.append(indentFor(depth))
            sb.append("</$prefix:Reason>")
            if (prettyPrint) sb.appendLine()
        }

        fault.getNode()?.let { node ->
            sb.append(indentFor(depth))
            sb.append("<$prefix:Node>")
            sb.append(escapeXml(node))
            sb.append("</$prefix:Node>")
            if (prettyPrint) sb.appendLine()
        }

        fault.getRole()?.let { role ->
            sb.append(indentFor(depth))
            sb.append("<$prefix:Role>")
            sb.append(escapeXml(role))
            sb.append("</$prefix:Role>")
            if (prettyPrint) sb.appendLine()
        }

        // Serialize detail if present
        fault.getDetail()?.let { detail ->
            sb.append(indentFor(depth + 1))
            sb.append("<$prefix:Detail>")
            if (prettyPrint) sb.appendLine()
            serializeElementContent(sb, detail, depth + 2)
            sb.append(indentFor(depth + 1))
            sb.append("</$prefix:Detail>")
            if (prettyPrint) sb.appendLine()
        }
    }

    private fun serializeSubcodes(sb: StringBuilder, subcodes: List<String>, prefix: String, depth: Int) {
        if (subcodes.isEmpty()) return

        val first = subcodes.first()
        sb.append(indentFor(depth))
        sb.append("<$prefix:Subcode>")
        if (prettyPrint) sb.appendLine()
        sb.append(indentFor(depth + 1))
        sb.append("<$prefix:Value>")
        sb.append(escapeXml(first))
        sb.append("</$prefix:Value>")
        if (prettyPrint) sb.appendLine()

        // Recursively serialize remaining subcodes
        serializeSubcodes(sb, subcodes.drop(1), prefix, depth + 1)

        sb.append(indentFor(depth))
        sb.append("</$prefix:Subcode>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeElementContent(sb: StringBuilder, holder: SoapElementHolder, depth: Int) {
        for (child in holder.getChildren()) {
            when (child) {
                is SoapElementBuilder -> serializeElement(sb, child, depth)
                is SoapListBuilder -> serializeList(sb, child, depth)
            }
        }
    }

    private fun serializeElement(sb: StringBuilder, element: SoapElementBuilder, depth: Int) {
        val name = element.getName() ?: return

        // Handle optional elements with no content
        if (element.isOptional() && !hasContent(element)) {
            return
        }

        // Handle nillable elements with no content
        if (element.isNillable() && !hasContent(element)) {
            sb.append(indentFor(depth))
            sb.append("<$name xsi:nil=\"true\"/>")
            if (prettyPrint) sb.appendLine()
            return
        }

        sb.append(indentFor(depth))
        sb.append("<$name")

        // Serialize attributes
        for ((attrName, attrValue) in element.getAttributes()) {
            sb.append(" $attrName=\"${escapeXmlAttr(attrValue)}\"")
        }

        // Check if element has content
        val hasChildren = element.getElements().isNotEmpty() || element.getLists().isNotEmpty()
        val hasCdata = element.cdata != null
        val hasRawXml = element.rawXml != null
        val hasTextContent = element.content != null

        if (!hasChildren && !hasCdata && !hasRawXml && !hasTextContent) {
            // Self-closing tag
            sb.append("/>")
            if (prettyPrint) sb.appendLine()
            return
        }

        sb.append(">")

        when {
            hasCdata -> {
                sb.append("<![CDATA[${element.cdata}]]>")
            }
            hasRawXml -> {
                if (prettyPrint) sb.appendLine()
                sb.append(element.rawXml)
                if (prettyPrint) sb.appendLine()
                sb.append(indentFor(depth))
            }
            hasChildren -> {
                if (prettyPrint) sb.appendLine()
                serializeElementContent(sb, element, depth + 1)
                sb.append(indentFor(depth))
            }
            hasTextContent -> {
                sb.append(escapeXml(element.content.toString()))
            }
        }

        sb.append("</$name>")
        if (prettyPrint) sb.appendLine()
    }

    private fun serializeList(sb: StringBuilder, list: SoapListBuilder, depth: Int) {
        val name = list.getName()

        // Check if list is completely empty (no elements or nested lists)
        if (list.getElements().isEmpty() && list.getLists().isEmpty()) {
            sb.append(indentFor(depth))
            sb.append("<$name/>")
            if (prettyPrint) sb.appendLine()
            return
        }

        sb.append(indentFor(depth))
        sb.append("<$name>")
        if (prettyPrint) sb.appendLine()

        serializeElementContent(sb, list, depth + 1)

        sb.append(indentFor(depth))
        sb.append("</$name>")
        if (prettyPrint) sb.appendLine()
    }

    private fun hasContent(element: SoapElementBuilder): Boolean {
        return element.content != null ||
                element.cdata != null ||
                element.rawXml != null ||
                element.getElements().isNotEmpty() ||
                element.getLists().isNotEmpty()
    }

    private fun indentFor(depth: Int): String {
        return if (prettyPrint) indent.repeat(depth) else ""
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun escapeXmlAttr(text: String): String {
        return escapeXml(text)
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

@JvmInline
value class SoapXml(val content: String)

/**
 * Extension function to convert a [SoapEnvelopeBuilder] to XML.
 */
fun SoapEnvelopeBuilder.toXml(prettyPrint: Boolean = true, indent: String = "  "): SoapXml {
    return SoapXmlSerializer(prettyPrint, indent).serialize(this).let(::SoapXml)
}
