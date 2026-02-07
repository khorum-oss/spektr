package org.khorum.oss.spektr.service

import jakarta.annotation.PostConstruct
import org.khorum.oss.spektr.dsl.EndpointModule
import org.khorum.oss.spektr.dsl.EndpointRegistry
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
    private val routerManager: DynamicRouterManager,
    @Value($$"${endpoint-jars.dir}")
    private val jarDir: String
) : Loggable {
    private var currentLoader: URLClassLoader? = null

    data class LoadResult(val count: Int, val jars: Int)

    fun reloadAll(): LoadResult {
        val dir = Path.of(jarDir)
        if (!dir.exists()) return LoadResult(0, 0)

        // Close previous classloader to prevent leaks
        currentLoader?.close()

        val jars = dir.listDirectoryEntries("*.jar").map { it.toUri().toURL() }
        val classLoader = URLClassLoader(jars.toTypedArray(), this::class.java.classLoader)
        currentLoader = classLoader

        val registry = EndpointRegistry()
        ServiceLoader.load(EndpointModule::class.java, classLoader)
            .forEach { module -> with(module) { registry.configure() } }

        routerManager.updateEndpoints(registry.endpoints)
        log.info("Loaded {} endpoints from {} JARs", registry.endpoints.size, jars.size)

        return LoadResult(registry.endpoints.size, jars.size)
    }

    private fun load() {
        log.info("Loading endpoints from JARs in {}", jarDir)
        val dir = Path.of(jarDir)

        if (!dir.exists()) {
            log.warn("JAR directory does not exist: {}", jarDir)
            routerManager.updateEndpoints(emptyList())
            return
        }

        val jars = dir.listDirectoryEntries("*.jar")
        if (jars.isEmpty()) {
            log.warn("No JAR files found in {}", jarDir)
            routerManager.updateEndpoints(emptyList())
            return
        }

        val jarUrls = jars.map { it.toUri().toURL() }
        val classLoader = URLClassLoader(jarUrls.toTypedArray(), this::class.java.classLoader)

        val registry = EndpointRegistry()
        val modules = ServiceLoader.load(EndpointModule::class.java, classLoader).toList()

        if (modules.isEmpty()) {
            log.warn("No EndpointModule implementations found in {} JARs", jars.size)
        }

        modules.forEach { module ->
            log.info("Loading endpoints from {}", module.javaClass.name)
            with(module) { registry.configure() }
        }

        routerManager.updateEndpoints(registry.endpoints)
        log.info("Loaded {} endpoints from {} JARs", registry.endpoints.size, jars.size)
    }

    @PostConstruct
    fun init() = load()
}