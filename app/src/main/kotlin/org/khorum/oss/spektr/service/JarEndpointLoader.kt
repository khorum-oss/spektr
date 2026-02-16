package org.khorum.oss.spektr.service

import jakarta.annotation.PostConstruct
import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.rest.RestEndpointRegistry
import org.khorum.oss.spektr.dsl.soap.SoapEndpointRegistry
import org.khorum.oss.spektr.utils.Loggable
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.ServiceLoader
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

@Component
class JarEndpointLoader(
    @Value("\${endpoint-jars.dir}")
    private val jarDir: String,
    private val routerManager: DynamicRouterManager? = null,
    private val soapRouterManager: SoapRouterManager? = null
) : Loggable {
    private var currentLoader: URLClassLoader? = null

    data class LoadResult(val count: Int, val jars: Int, val soapCount: Int = 0)

    fun reloadAll(): LoadResult {
        val dir = Path.of(jarDir)
        if (!dir.exists()) return LoadResult(0, 0, 0)

        // Close previous classloader to prevent leaks
        currentLoader?.close()

        val jars = dir.listDirectoryEntries("*.jar").map { it.toUri().toURL() }
        val classLoader = URLClassLoader(jars.toTypedArray(), this::class.java.classLoader)
        currentLoader = classLoader

        val modules = ServiceLoader.load(EndpointModule::class.java, classLoader).toList()

        val restCount = if (routerManager != null) {
            val registry = RestEndpointRegistry()
            modules.forEach { module -> with(module) { registry.configure() } }
            routerManager.updateEndpoints(registry.endpoints)
            log.info("Loaded {} REST endpoints from {} JARs", registry.endpoints.size, jars.size)
            registry.endpoints.size
        } else 0

        val soapCount = if (soapRouterManager != null) {
            val soapRegistry = SoapEndpointRegistry()
            modules.forEach { module -> with(module) { soapRegistry.configureSoap() } }
            soapRouterManager.updateEndpoints(soapRegistry.endpoints)
            log.info("Loaded {} SOAP endpoints from {} JARs", soapRegistry.endpoints.size, jars.size)
            soapRegistry.endpoints.size
        } else 0

        return LoadResult(restCount, jars.size, soapCount)
    }

    private fun load() {
        log.info("Loading endpoints from JARs in {}", jarDir)
        val dir = Path.of(jarDir)

        if (!dir.exists()) {
            log.warn("JAR directory does not exist: {}", jarDir)
            routerManager?.updateEndpoints(emptyList())
            soapRouterManager?.updateEndpoints(emptyList())
            return
        }

        val jars = dir.listDirectoryEntries("*.jar")
        if (jars.isEmpty()) {
            log.warn("No JAR files found in {}", jarDir)
            routerManager?.updateEndpoints(emptyList())
            soapRouterManager?.updateEndpoints(emptyList())
            return
        }

        val jarUrls = jars.map { it.toUri().toURL() }
        val classLoader = URLClassLoader(jarUrls.toTypedArray(), this::class.java.classLoader)

        val modules = mutableListOf<EndpointModule>()
        try {
            val serviceLoader = ServiceLoader.load(EndpointModule::class.java, classLoader)
            for (module in serviceLoader) {
                log.info("Found EndpointModule: {}", module.javaClass.name)
                modules.add(module)
            }
        } catch (e: Exception) {
            log.error("Error loading EndpointModule implementations: {}", e.message, e)
        }

        if (modules.isEmpty()) {
            log.warn("No EndpointModule implementations found in {} JARs", jars.size)
            // Log the JARs being scanned
            jars.forEach { jar -> log.debug("Scanned JAR: {}", jar) }
        }

        if (routerManager != null) {
            val registry = RestEndpointRegistry()
            modules.forEach { module ->
                log.info("Loading REST endpoints from {}", module.javaClass.name)
                with(module) { registry.configure() }
            }
            routerManager.updateEndpoints(registry.endpoints)
            log.info("Loaded {} REST endpoints from {} JARs", registry.endpoints.size, jars.size)
            logRestEndpoints(registry)
        }

        if (soapRouterManager != null) {
            val soapRegistry = SoapEndpointRegistry()
            modules.forEach { module ->
                log.info("Loading SOAP endpoints from {}", module.javaClass.name)
                with(module) { soapRegistry.configureSoap() }
            }
            soapRouterManager.updateEndpoints(soapRegistry.endpoints)
            log.info("Loaded {} SOAP endpoints from {} JARs", soapRegistry.endpoints.size, jars.size)
            logSoapEndpoints(soapRegistry)
        }
    }

    private fun logRestEndpoints(registry: RestEndpointRegistry) {
        if (registry.endpoints.isEmpty()) return
        log.info("=== REST Endpoints ===")
        registry.endpoints.forEach { endpoint ->
            log.info("  {} {}", endpoint.method, endpoint.path)
        }
    }

    private fun logSoapEndpoints(soapRegistry: SoapEndpointRegistry) {
        if (soapRegistry.endpoints.isEmpty()) return
        log.info("=== SOAP Endpoints ===")
        soapRegistry.endpoints.forEach { endpoint ->
            log.info("  POST {} (SOAPAction: {})", endpoint.path, endpoint.soapAction)
        }
    }

    @PostConstruct
    fun init() = load()
}
