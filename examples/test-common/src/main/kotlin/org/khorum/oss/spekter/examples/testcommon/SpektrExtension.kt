package org.khorum.oss.spekter.examples.testcommon

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.util.concurrent.ConcurrentHashMap

class SpektrExtension : BeforeAllCallback, ParameterResolver {
    companion object {
        private val containers = ConcurrentHashMap<String, SpektrContainer>()
    }

    override fun beforeAll(context: ExtensionContext) {
        val annotation = context.requiredTestClass.getAnnotation(WithSpektr::class.java) ?: return

        val container = containers.computeIfAbsent(annotation.endpointJarsPath) {
            SpektrContainer(annotation.image).apply {
                if (annotation.endpointJarsPath.isNotBlank()) {
                    withEndpointJars(annotation.endpointJarsPath)
                }
                withRestEnabled(annotation.restEnabled)
                withSoapEnabled(annotation.soapEnabled)
                start()
            }
        }

        System.setProperty("spektr.base-url", container.baseUrl)
        annotation.properties.forEach { prop ->
            System.setProperty(prop, container.baseUrl)
        }
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Boolean = parameterContext.parameter.type == SpektrContainer::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Any? {
        val annotation = extensionContext.requiredTestClass.getAnnotation(WithSpektr::class.java)
        return containers[annotation?.endpointJarsPath ?: ""]
    }
}