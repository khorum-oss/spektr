package org.khorum.oss.spekter.examples.common

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ghost", propOrder = ["type", "origin"])
data class Ghost(
    @field:XmlElement(required = true)
    val type: String,
    @field:XmlElement(required = true)
    val origin: String
) {
    companion object {
        const val NAMESPACE = "http://org.khorum-oss.com/ghost-book"
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostRequest", propOrder = ["type", "origin"])
@XmlRootElement(name = "createGhostRequest", namespace = Ghost.NAMESPACE)
data class CreateGhostRequest(
    @field:XmlElement(nillable = true)
    val type: String? = null,
    @field:XmlElement(nillable = true)
    val origin: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostResponse", propOrder = ["ghost"])
@XmlRootElement(name = "createGhostResponse", namespace = Ghost.NAMESPACE)
data class CreateGhostResponse(
    @field:XmlElement(required = true)
    val ghost: Ghost = Ghost("", "")
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostRequest", propOrder = ["type"])
@XmlRootElement(name = "getGhostRequest", namespace = Ghost.NAMESPACE)
data class GetGhostRequest(
    @field:XmlElement(required = true)
    val type: String = ""
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostResponse", propOrder = ["ghost"])
@XmlRootElement(name = "getGhostResponse", namespace = Ghost.NAMESPACE)
data class GetGhostResponse(
    @field:XmlElement(required = false)
    val ghost: Ghost? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsRequest")
@XmlRootElement(name = "listGhostsRequest", namespace = Ghost.NAMESPACE)
class ListGhostsRequest  // no fields needed, but you could add filters here

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsResponse", propOrder = ["ghosts"])
@XmlRootElement(name = "listGhostsResponse", namespace = Ghost.NAMESPACE)
data class ListGhostsResponse(
    @field:XmlElement(name = "ghost")
    val ghosts: List<Ghost> = emptyList()
)