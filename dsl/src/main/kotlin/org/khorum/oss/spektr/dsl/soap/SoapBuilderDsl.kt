package org.khorum.oss.spektr.dsl.soap

// ── Envelope builder: clean top-level API ──────────────

@DslMarker
annotation class SoapDslMarker

@SoapDslMarker
fun soapEnvelope(block: SoapEnvelopeBuilder.() -> Unit): SoapEnvelopeBuilder {
    return SoapEnvelopeBuilder().apply(block)
}

class SoapEnvelopeBuilder {
    var version: SoapVersion = SoapVersion.V1_2
    var envelopePrefix: String = "soapenv"
    var schemasLocation: String? = null
    private var namespaces: SoapNamespacesBuilder? = null
    private var header: SoapHeaderBuilder? = null
    private var body: SoapBodyHolder? = null

    // Internal getters for serialization
    internal fun getNamespaces(): SoapNamespacesBuilder? = namespaces
    internal fun getHeader(): SoapHeaderBuilder? = header
    internal fun getBody(): SoapBodyHolder? = body

    @SoapDslMarker
    fun namespaces(block: SoapNamespacesBuilder.() -> Unit) {
        namespaces = SoapNamespacesBuilder().apply(block)
    }

    @SoapDslMarker
    fun header(block: SoapHeaderBuilder.() -> Unit) {
        header = SoapHeaderBuilder().apply(block)
    }

    @SoapDslMarker
    fun body(block: SoapBodyBuilder.() -> Unit) {
        checkBodyNotSet()
        body = SoapBodyBuilder(version).apply(block)
    }

    @SoapDslMarker
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
    fun code(value: String)
    @SoapDslMarker
    fun code(block: SoapFaultCode.() -> Unit)
    @SoapDslMarker
    fun reason(block: SoapFaultReason.() -> Unit)
    fun node(node: String)
    fun role(role: String)
}

// ── Sealed hierarchy: what gets built ──────────────────

sealed class SoapFaultBuilder : SoapBodyHolder, SoapFaultScope {
    protected var detail: SoapElementBuilder? = null

    // Internal getter for serialization
    internal fun getDetail(): SoapElementBuilder? = detail

    override fun detail(block: SoapElementBuilder.() -> Unit) {
        detail = SoapElementBuilder().apply(block)
    }

    // Default no-ops — overridden by the relevant subclass
    override fun faultCode(code: String): Unit = versionMismatch("faultCode", "1.1")
    override fun faultString(reason: String): Unit = versionMismatch("faultString", "1.1")
    override fun faultActor(actor: String): Unit = versionMismatch("faultActor", "1.1")
    override fun code(value: String): Unit = versionMismatch("code", "1.2")
    override fun code(block: SoapFaultCode.() -> Unit): Unit = versionMismatch("code", "1.2")
    override fun reason(block: SoapFaultReason.() -> Unit): Unit = versionMismatch("reason", "1.2")
    override fun node(node: String): Unit = versionMismatch("node", "1.2")
    override fun role(role: String): Unit = versionMismatch("role", "1.2")

    private fun versionMismatch(method: String, requiredVersion: String): Nothing =
        throw IllegalStateException("$method requires SOAP $requiredVersion")
}

class Soap11FaultBuilder : SoapFaultBuilder() {
    private var faultCode: String? = null
    private var faultString: String? = null
    private var faultActor: String? = null

    // Internal getters for serialization
    internal fun getFaultCode(): String? = faultCode
    internal fun getFaultString(): String? = faultString
    internal fun getFaultActor(): String? = faultActor

    override fun faultCode(code: String) { faultCode = code }
    override fun faultString(reason: String) { faultString = reason }
    override fun faultActor(actor: String) { faultActor = actor }
}

class Soap12FaultBuilder : SoapFaultBuilder() {
    private var code: SoapFaultCode? = null
    private var reason: SoapFaultReason? = null
    private var node: String? = null
    private var role: String? = null

    // Internal getters for serialization
    internal fun getCode(): SoapFaultCode? = code
    internal fun getReason(): SoapFaultReason? = reason
    internal fun getNode(): String? = node
    internal fun getRole(): String? = role

    override fun code(value: String) {
        code = SoapFaultCode().apply { value(value) }
    }
    override fun code(block: SoapFaultCode.() -> Unit) { code = SoapFaultCode().apply(block) }
    override fun reason(block: SoapFaultReason.() -> Unit) { reason = SoapFaultReason().apply(block) }
    override fun node(node: String) { this.node = node }
    override fun role(role: String) { this.role = role }
}

class SoapFaultCode {
    private var value: String? = null
    private val subcodes: MutableList<String> = mutableListOf()

    // Internal getters for serialization
    internal fun getValue(): String? = value
    internal fun getSubcodes(): List<String> = subcodes

    fun value(code: String) { value = code }
    fun value(namespace: String, code: String) = value("$namespace:$code")
    fun subcode(code: String) { subcodes.add(code) }
    fun subcode(namespace: String, code: String) = subcode("$namespace:$code")
}

class SoapFaultReason {
    var text: String? = null
    var lang: String? = null
}

class SoapNamespacesBuilder {
    private val namespaces: MutableMap<String, String> = mutableMapOf()

    // Internal getter for serialization
    internal fun getNamespaces(): Map<String, String> = namespaces

    fun ns(prefix: String, uri: String) {
        namespaces[prefix] = uri
    }

    fun ns(entry: Pair<String, String>) = ns(entry.first, entry.second)
}

/** Marker interface for child nodes that can be serialized in order */
sealed interface SoapChild

abstract class SoapElementHolder {
    protected val attributes: MutableMap<String, String> = mutableMapOf()
    protected val children: MutableList<SoapChild> = mutableListOf()
    var cdata: String? = null
    var rawXml: String? = null

    // Internal getters for serialization
    internal fun getAttributes(): Map<String, String> = attributes
    internal fun getChildren(): List<SoapChild> = children

    // Convenience getters for backwards compatibility
    internal fun getElements(): List<SoapElementBuilder> = children.filterIsInstance<SoapElementBuilder>()
    internal fun getLists(): List<SoapListBuilder> = children.filterIsInstance<SoapListBuilder>()

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

class SoapHeaderBuilder : SoapElementHolder()

sealed interface SoapBodyHolder

class SoapBodyBuilder(private val version: SoapVersion) : SoapBodyHolder, SoapElementHolder() {
    private var fault: SoapFaultBuilder? = null

    // Internal getter for serialization
    internal fun getFault(): SoapFaultBuilder? = fault

    @SoapDslMarker
    fun fault(block: SoapFaultScope.() -> Unit) {
        fault = version.faultBuilder().apply(block)
    }
}

class SoapElementBuilder(
    private var name: String? = null,
    private val optional: Boolean = false,
    private val nillable: Boolean = false
) : SoapElementHolder(), SoapChild {
    var content: Any? = null

    // Internal getters for serialization
    internal fun getName(): String? = name
    internal fun isOptional(): Boolean = optional
    internal fun isNillable(): Boolean = nillable
}

class SoapListBuilder(private val name: String) : SoapElementHolder(), SoapChild {
    // Internal getter for serialization
    internal fun getName(): String = name
}