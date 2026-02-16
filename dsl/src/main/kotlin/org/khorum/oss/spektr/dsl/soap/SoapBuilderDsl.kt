package org.khorum.oss.spektr.dsl.soap

// ── Envelope builder: clean top-level API ──────────────

fun soapEnvelope(block: SoapEnvelopeBuilder.() -> Unit): SoapEnvelopeBuilder {
    return SoapEnvelopeBuilder().apply(block)
}

class SoapEnvelopeBuilder {
    var version: SoapVersion = SoapVersion.V1_2
    var envelopePrefix: String = "soapenv"
    private var namespaces: SoapNamespacesBuilder? = null
    private var header: SoapHeaderBuilder? = null
    private var body: SoapBodyHolder? = null

    fun namespaces(block: SoapNamespacesBuilder.() -> Unit) {
        namespaces = SoapNamespacesBuilder().apply(block)
    }

    fun header(block: SoapHeaderBuilder.() -> Unit) {
        header = SoapHeaderBuilder().apply(block)
    }

    fun body(block: SoapBodyBuilder.() -> Unit) {
        checkBodyNotSet()
        body = SoapBodyBuilder().apply(block)
    }

    fun fault(block: SoapFaultScope.() -> Unit) {
        checkBodyNotSet()
        body = version.faultBuilder().apply(block)
    }

    private fun checkBodyNotSet() {
        if (body != null) throw IllegalStateException("Body already set")
    }
}

// ── Version drives fault construction ──────────────────

enum class SoapVersion {
    V1_1 {
        override fun faultBuilder() = Soap11FaultBuilder()
    },
    V1_2 {
        override fun faultBuilder() = Soap12FaultBuilder()
    };

    abstract fun faultBuilder(): SoapFaultBuilder
}

// ── Fault scope: what callers see ──────────────────────
// Common surface shared by both versions.
// Version-specific methods just no-op or throw on the wrong version,
// OR you split into two blocks — see the alternative below.

interface SoapFaultScope {
    fun detail(block: SoapElementBuilder.() -> Unit)

    // 1.1 methods
    fun faultCode(code: String)
    fun faultString(reason: String)
    fun faultActor(actor: String)

    // 1.2 methods
    fun code(block: SoapFaultCode.() -> Unit)
    fun reason(content: String)
    fun node(node: String)
    fun role(role: String)
}

// ── Sealed hierarchy: what gets built ──────────────────

sealed class SoapFaultBuilder : SoapBodyHolder, SoapFaultScope {
    protected var detail: SoapElementBuilder? = null

    override fun detail(block: SoapElementBuilder.() -> Unit) {
        detail = SoapElementBuilder().apply(block)
    }

    // Default no-ops — overridden by the relevant subclass
    override fun faultCode(code: String): Unit = versionMismatch("faultCode", "1.1")
    override fun faultString(reason: String): Unit = versionMismatch("faultString", "1.1")
    override fun faultActor(actor: String): Unit = versionMismatch("faultActor", "1.1")
    override fun code(block: SoapFaultCode.() -> Unit): Unit = versionMismatch("code", "1.2")
    override fun reason(content: String): Unit = versionMismatch("reason", "1.2")
    override fun node(node: String): Unit = versionMismatch("node", "1.2")
    override fun role(role: String): Unit = versionMismatch("role", "1.2")

    private fun versionMismatch(method: String, requiredVersion: String): Nothing =
        throw IllegalStateException("$method requires SOAP $requiredVersion")
}

class Soap11FaultBuilder : SoapFaultBuilder() {
    private var faultCode: String? = null
    private var faultString: String? = null
    private var faultActor: String? = null

    override fun faultCode(code: String) { faultCode = code }
    override fun faultString(reason: String) { faultString = reason }
    override fun faultActor(actor: String) { faultActor = actor }
}

class Soap12FaultBuilder : SoapFaultBuilder() {
    private var code: SoapFaultCode? = null
    private var reason: String? = null
    private var node: String? = null
    private var role: String? = null

    override fun code(block: SoapFaultCode.() -> Unit) { code = SoapFaultCode().apply(block) }
    override fun reason(content: String) { reason = content }
    override fun node(node: String) { this.node = node }
    override fun role(role: String) { this.role = role }
}

class SoapFaultCode {
    private var value: String? = null
    private val subcodes: MutableList<String> = mutableListOf()

    fun value(code: String) { value = code }
    fun value(namespace: String, code: String) = value("$namespace:$code")
    fun subcode(code: String) { subcodes.add(code) }
    fun subcode(namespace: String, code: String) = subcode("$namespace:$code")
}

class SoapNamespacesBuilder {
    private val namespaces: MutableMap<String, String> = mutableMapOf()

    fun ns(prefix: String, uri: String) {
        namespaces[prefix] = uri
    }

    fun ns(entry: Pair<String, String>) = ns(entry.first, entry.second)
}

abstract class SoapElementHolder {
    protected val attributes: MutableMap<String, String> = mutableMapOf()
    protected val elements: MutableList<SoapElementBuilder> = mutableListOf()
    protected val lists: MutableList<SoapListBuilder> = mutableListOf()
    var cdata: String? = null
    var rawXml: String? = null

    fun element(name: String, block: SoapElementBuilder.() -> Unit) {
        elements.add(SoapElementBuilder(name).apply(block))
    }

    fun element(
        namespace: String,
        name: String,
        block: SoapElementBuilder.() -> Unit
    ) = element("$namespace:$name", block)

    fun attribute(name: String, value: String) {
        attributes[name] = value
    }

    fun attributes(vararg pairs: Pair<String, String>) {
        attributes.putAll(pairs)
    }

    fun optional(name: String, block: SoapElementBuilder.() -> Unit) {
        elements.add(SoapElementBuilder(name, optional = true).apply(block))
    }

    fun optional(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        optional("$namespace:$name", block)

    fun nillable(name: String, block: SoapElementBuilder.() -> Unit) {
        elements.add(SoapElementBuilder(name, nillable = true).apply(block))
    }

    fun nillable(namespace: String, name: String, block: SoapElementBuilder.() -> Unit) =
        nillable("$namespace:$name", block)

    fun list(name: String, block: SoapListBuilder.() -> Unit) {
        lists.add(SoapListBuilder(name).apply(block))
    }
}

class SoapHeaderBuilder : SoapElementHolder()

sealed interface SoapBodyHolder

class SoapBodyBuilder : SoapBodyHolder, SoapElementHolder()

class SoapElementBuilder(
    private var name: String? = null,
    private val optional: Boolean = false,
    private val nillable: Boolean = false
) : SoapElementHolder() {
    var text: String? = null
}

class SoapListBuilder(private val name: String)