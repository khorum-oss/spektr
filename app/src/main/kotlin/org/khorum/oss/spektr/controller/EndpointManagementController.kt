package org.khorum.oss.spektr.controller

import org.khorum.oss.spektr.service.JarEndpointLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

@RestController
@RequestMapping("/admin/endpoints")
class EndpointManagementController(
    @Value($$"${endpoint-jars.dir}") private val jarDir: String,
    private val jarLoader: JarEndpointLoader
) {
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAndReload(@RequestPart("jar") file: MultipartFile): ResponseEntity<ReloadResult> {
        val filename = file.originalFilename?.takeIf { it.isNotBlank() }
            ?: "uploaded-${System.currentTimeMillis()}.jar"
        val target = Path.of(jarDir, filename)
        file.transferTo(target)

        return reload()
    }
    
    @PostMapping("/reload")
    fun reload(): ResponseEntity<ReloadResult> {
        val before = System.currentTimeMillis()
        val result = jarLoader.reloadAll()
        val elapsed = System.currentTimeMillis() - before
        
        return ResponseEntity.ok(ReloadResult(
            endpointsLoaded = result.count,
            jarsProcessed = result.jars,
            reloadTimeMs = elapsed
        ))
    }
}

data class ReloadResult(
    val endpointsLoaded: Int,
    val jarsProcessed: Int,
    val reloadTimeMs: Long
)