package org.khorum.spektr.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.khorum.oss.spektr.controller.EndpointManagementController
import org.khorum.oss.spektr.service.JarEndpointLoader
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndpointManagementControllerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `reload returns endpoint and jar counts`() {
        val jarLoader = mock<JarEndpointLoader>()
        whenever(jarLoader.reloadAll()).thenReturn(JarEndpointLoader.LoadResult(5, 2))

        val controller = EndpointManagementController(tempDir.toString(), jarLoader)
        val response = controller.reload()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(5, response.body?.endpointsLoaded)
        assertEquals(2, response.body?.jarsProcessed)
        assertTrue(response.body?.reloadTimeMs!! >= 0)
        verify(jarLoader).reloadAll()
    }

    @Test
    fun `reload returns zero when no jars found`() {
        val jarLoader = mock<JarEndpointLoader>()
        whenever(jarLoader.reloadAll()).thenReturn(JarEndpointLoader.LoadResult(0, 0))

        val controller = EndpointManagementController(tempDir.toString(), jarLoader)
        val response = controller.reload()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.endpointsLoaded)
        assertEquals(0, response.body?.jarsProcessed)
    }

    @Test
    fun `upload saves file and triggers reload`() {
        val jarLoader = mock<JarEndpointLoader>()
        whenever(jarLoader.reloadAll()).thenReturn(JarEndpointLoader.LoadResult(3, 1))

        val controller = EndpointManagementController(tempDir.toString(), jarLoader)
        val file = MockMultipartFile(
            "jar",
            "test-endpoints.jar",
            "application/java-archive",
            "fake jar content".toByteArray()
        )

        val response = controller.uploadAndReload(file)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(3, response.body?.endpointsLoaded)
        assertTrue(tempDir.resolve("test-endpoints.jar").toFile().exists())
        verify(jarLoader).reloadAll()
    }

    @Test
    fun `upload uses generated filename when original is empty`() {
        val jarLoader = mock<JarEndpointLoader>()
        whenever(jarLoader.reloadAll()).thenReturn(JarEndpointLoader.LoadResult(1, 1))

        val controller = EndpointManagementController(tempDir.toString(), jarLoader)
        val file = MockMultipartFile(
            "jar",
            "",
            "application/java-archive",
            "fake jar content".toByteArray()
        )

        val response = controller.uploadAndReload(file)

        assertEquals(HttpStatus.OK, response.statusCode)
        val files = tempDir.toFile().listFiles()
        assertTrue(files?.any { it.name.startsWith("uploaded-") && it.name.endsWith(".jar") } == true)
    }
}