package org.khorum.oss.spektr.hauntedhousetracker.config

import org.khorum.oss.spekter.examples.common.domain.Ghost
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.context.MessageContext
import java.io.ByteArrayOutputStream

private val log = LoggerFactory.getLogger(GhostSoapClientConfig::class.java)

@Configuration
class GhostSoapClientConfig(
    @Value($$"${ghost-book.ws.endpoint.uri}")
    private val ghostBookEndpointUri: String
) {

    @Bean
    fun marshaller(): Jaxb2Marshaller {
        return Jaxb2Marshaller().apply {
            contextPath = Ghost.PACKAGE
        }
    }

    @Bean
    fun loggingInterceptor(): ClientInterceptor = object : ClientInterceptor {
        override fun handleRequest(messageContext: MessageContext): Boolean {
            val out = ByteArrayOutputStream()
            messageContext.request.writeTo(out)
            log.info("SOAP Request:\n{}", out.toString(Charsets.UTF_8))
            return true
        }

        override fun handleResponse(messageContext: MessageContext): Boolean {
            val out = ByteArrayOutputStream()
            messageContext.response.writeTo(out)
            log.info("SOAP Response:\n{}", out.toString(Charsets.UTF_8))
            return true
        }

        override fun handleFault(messageContext: MessageContext): Boolean {
            val out = ByteArrayOutputStream()
            messageContext.response.writeTo(out)
            log.error("SOAP Fault:\n{}", out.toString(Charsets.UTF_8))
            return true
        }

        override fun afterCompletion(messageContext: MessageContext, ex: Exception?) {
            ex?.let { log.error("SOAP call failed", it) }
        }
    }

    @Bean
    fun ghostWebServiceTemplate(marshaller: Jaxb2Marshaller, loggingInterceptor: ClientInterceptor): WebServiceTemplate {
        return WebServiceTemplate().apply {
            setDefaultUri(ghostBookEndpointUri)
            setMarshaller(marshaller)
            setUnmarshaller(marshaller)
            setInterceptors(arrayOf(loggingInterceptor))
        }
    }
}