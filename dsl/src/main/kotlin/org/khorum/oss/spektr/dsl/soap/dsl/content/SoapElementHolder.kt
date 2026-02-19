package org.khorum.oss.spektr.dsl.soap.dsl.content

import org.khorum.oss.spektr.dsl.soap.dsl.SoapDslMarker

abstract class SoapElementHolder {
    protected val attributes: MutableMap<String, String> = mutableMapOf()
    protected val children: MutableList<SoapChild> = mutableListOf()
    var cdata: String? = null
    var rawXml: String? = null

    internal fun serializeContent(sb: StringBuilder, pretty: Boolean, indent: String, depth: Int) {
        for (child in children) {
            when (child) {
                is SoapElementBuilder -> child.serialize(sb, pretty, indent, depth)
                is SoapListBuilder -> child.serialize(sb, pretty, indent, depth)
            }
        }
    }

    @SoapDslMarker
    fun element(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name).apply(block))
    }

    @SoapDslMarker
    fun element(
        namespace: String,
        name: String,
        block: SoapElementBuilder.() -> Unit
    ) = element("$namespace:$name", block)

    fun attribute(name: String, value: Any) {
        attributes[name] = value.toString()
    }

    fun attribute(namespace: String, name: String, value: String) = attribute(
        "$namespace:$name", value
    )

    fun attributes(vararg pairs: Pair<String, String>) {
        attributes.putAll(pairs)
    }

    @SoapDslMarker
    fun optional(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name, optional = true).apply(block))
    }

    @SoapDslMarker
    fun optional(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        optional("$namespace:$name", block)

    @SoapDslMarker
    fun nillable(name: String, block: SoapElementBuilder.() -> Unit) {
        children.add(SoapElementBuilder(name, nillable = true).apply(block))
    }

    @SoapDslMarker
    fun nillable(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        nillable("$namespace:$name", block)

    @SoapDslMarker
    fun list(name: String, block: SoapListBuilder.() -> Unit) {
        children.add(SoapListBuilder(name).apply(block))
    }

    @SoapDslMarker
    fun list(namespace: String, name: String, block: SoapListBuilder.() -> Unit) {
        children.add(SoapListBuilder("$namespace:$name").apply(block))
    }
}