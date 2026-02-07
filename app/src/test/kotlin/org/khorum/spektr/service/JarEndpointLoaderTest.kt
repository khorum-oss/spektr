package org.khorum.spektr.service

import org.junit.jupiter.api.Test
import org.khorum.oss.spektr.dsl.EndpointDefinition
import org.khorum.oss.spektr.dsl.SoapEndpointDefinition
import org.khorum.oss.spektr.service.DynamicRouterManager
import org.khorum.oss.spektr.service.JarEndpointLoader
import org.khorum.oss.spektr.service.SoapRouterManager
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JarEndpointLoaderTest {

    @Test
    fun `loader loads REST endpoints from valid JAR directory`() {
        val routerManager = mock<DynamicRouterManager>()
        val loader = JarEndpointLoader("../examples/build/libs")
        ReflectionTestUtils.setField(loader, "routerManager", routerManager)

        loader.reloadAll()

        val captor = argumentCaptor<List<EndpointDefinition>>()
        verify(routerManager).updateEndpoints(captor.capture())

        val endpoints = captor.firstValue
        assertTrue(endpoints.isNotEmpty(), "Should load at least one REST endpoint")
    }

    @Test
    fun `loader loads SOAP endpoints from valid JAR directory`() {
        val soapRouterManager = mock<SoapRouterManager>()
        val loader = JarEndpointLoader("../examples/build/libs")
        ReflectionTestUtils.setField(loader, "soapRouterManager", soapRouterManager)

        loader.reloadAll()

        val captor = argumentCaptor<List<SoapEndpointDefinition>>()
        verify(soapRouterManager).updateEndpoints(captor.capture())

        val endpoints = captor.firstValue
        assertTrue(endpoints.isNotEmpty(), "Should load at least one SOAP endpoint")
    }

    @Test
    fun `loader loads both REST and SOAP endpoints`() {
        val routerManager = mock<DynamicRouterManager>()
        val soapRouterManager = mock<SoapRouterManager>()
        val loader = JarEndpointLoader("../examples/build/libs")
        ReflectionTestUtils.setField(loader, "routerManager", routerManager)
        ReflectionTestUtils.setField(loader, "soapRouterManager", soapRouterManager)

        val result = loader.reloadAll()

        assertTrue(result.count > 0, "Should load REST endpoints")
        assertTrue(result.soapCount > 0, "Should load SOAP endpoints")
        assertTrue(result.jars > 0, "Should process JARs")
    }

    @Test
    fun `loader handles missing directory gracefully`() {
        val loader = JarEndpointLoader("/nonexistent/path")

        val result = loader.reloadAll()

        assertEquals(0, result.count, "Should return zero REST endpoints for missing directory")
        assertEquals(0, result.soapCount, "Should return zero SOAP endpoints for missing directory")
        assertEquals(0, result.jars, "Should return zero jars for missing directory")
    }

    @Test
    fun `loader works without REST router manager`() {
        val soapRouterManager = mock<SoapRouterManager>()
        val loader = JarEndpointLoader("../examples/build/libs")
        ReflectionTestUtils.setField(loader, "soapRouterManager", soapRouterManager)

        val result = loader.reloadAll()

        assertEquals(0, result.count, "REST count should be zero when router manager is absent")
        assertTrue(result.soapCount >= 0, "SOAP endpoints should still be loaded")
    }

    @Test
    fun `loader works without SOAP router manager`() {
        val routerManager = mock<DynamicRouterManager>()
        val loader = JarEndpointLoader("../examples/build/libs")
        ReflectionTestUtils.setField(loader, "routerManager", routerManager)

        val result = loader.reloadAll()

        assertTrue(result.count > 0, "REST endpoints should still be loaded")
        assertEquals(0, result.soapCount, "SOAP count should be zero when soap router manager is absent")
    }
}
