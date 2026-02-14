package org.khorum.oss.spektr.ghostbook.config

import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema
import org.springframework.xml.xsd.XsdSchema

@EnableWs
@Configuration
class WebServiceConfig {

    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext): ServletRegistrationBean<MessageDispatcherServlet> {
        val servlet = MessageDispatcherServlet().apply {
            setApplicationContext(applicationContext)
            isTransformWsdlLocations = true
        }
        return ServletRegistrationBean(servlet, "/ws/*")
    }

    @Bean(name = ["ghosts"])
    fun defaultWsdl11Definition(ghostsSchema: XsdSchema): DefaultWsdl11Definition {
        return DefaultWsdl11Definition().apply {
            setPortTypeName("GhostsPort")
            setLocationUri("/ws")
            setTargetNamespace(Ghost.NAMESPACE)
            setSchema(ghostsSchema)
        }
    }

    @Bean
    fun ghostsSchema(): XsdSchema {
        // XSD is auto-generated at build time from JAXB-annotated Ghost classes
        // Run: ./gradlew :examples:ghost-book:generateXsd
        return SimpleXsdSchema(ClassPathResource("xsd/ghosts.xsd"))
    }
}