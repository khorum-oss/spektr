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

    fun reloadAll() = load()

    private fun load() {
        log.info("Loading endpoints from JARs in {}", jarDir)
        val dir = Path.of(jarDir)
        if (!dir.exists()) return run { log.info("No JARs found in {}", jarDir) }

        val jars = dir.listDirectoryEntries("*.jar").map { it.toUri().toURL() }
        val classLoader = URLClassLoader(jars.toTypedArray(), this::class.java.classLoader)

        val registry = EndpointRegistry()
        ServiceLoader.load(EndpointModule::class.java, classLoader)
            .onEach { log.info("Loaded {}", it.javaClass.name) }
            .forEach { module -> with(module) { registry.configure() } }

        routerManager.updateEndpoints(registry.endpoints)
        log.info("Loaded {} endpoints from {} JARs", registry.endpoints.size, jars.size)
    }

    @PostConstruct
    fun init() = load()
}