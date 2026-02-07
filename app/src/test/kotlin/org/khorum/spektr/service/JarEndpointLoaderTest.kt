package org.khorum.spektr.service

import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.service.DynamicRouterManager
import org.khorum.oss.spektr.service.JarEndpointLoader
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.khorum.oss.spektr.dsl.EndpointDefinition
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JarEndpointLoaderTest {

    @Test
    fun `loader loads endpoints from valid JAR directory`() {
        val routerManager = mock<DynamicRouterManager>()
        val loader = JarEndpointLoader(routerManager, "../examples/build/libs")

        loader.reloadAll()

        val captor = argumentCaptor<List<EndpointDefinition>>()
        verify(routerManager).updateEndpoints(captor.capture())

        val endpoints = captor.firstValue
        assertTrue(endpoints.isNotEmpty(), "Should load at least one endpoint")
    }

    @Test
    fun `loader handles missing directory gracefully`() {
        val routerManager = mock<DynamicRouterManager>()
        val loader = JarEndpointLoader(routerManager, "/nonexistent/path")

        val result = loader.reloadAll()

        assertEquals(0, result.count, "Should return zero endpoints for missing directory")
        assertEquals(0, result.jars, "Should return zero jars for missing directory")
    }
}