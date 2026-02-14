package org.khorum.oss.spekter.examples.common.domain

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ghost", propOrder = ["type", "origin"])
data class Ghost(
    @field:XmlElement(required = true)
    val type: String = "",
    @field:XmlElement(nillable = true)
    val origin: String? = null
) {
    companion object {
        const val NAMESPACE = "http://org.khorum-oss.com/ghost-book"
        const val PACKAGE = "org.khorum.oss.spekter.examples.common"
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostRequest", propOrder = ["type", "origin"])
@XmlRootElement(name = "CreateGhostRequest", namespace = Ghost.NAMESPACE)
data class CreateGhostRequest(
    @field:XmlElement(nillable = true)
    val type: String? = null,
    @field:XmlElement(nillable = true)
    val origin: String? = null,
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "addresses", nillable = true)
    val addresses: List<Address>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostResponse", propOrder = ["ghost"])
@XmlRootElement(name = "CreateGhostResponse", namespace = Ghost.NAMESPACE)
data class CreateGhostResponse(
    @field:XmlElement(required = true)
    val ghost: Ghost = Ghost(""),
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "houses", nillable = true)
    val houses: List<HauntedHouse>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostRequest", propOrder = ["type"])
@XmlRootElement(name = "GetGhostRequest", namespace = Ghost.NAMESPACE)
data class GetGhostRequest(
    @field:XmlElement(required = true)
    val type: String = "",
    @field:XmlElement(nillable = true)
    val includeHouses: Boolean = false
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostResponse", propOrder = ["ghost"])
@XmlRootElement(name = "GetGhostResponse", namespace = Ghost.NAMESPACE)
data class GetGhostResponse(
    @field:XmlElement(required = false)
    val ghost: Ghost? = null,
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "houses", nillable = true)
    val houses: List<HauntedHouse>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsRequest")
@XmlRootElement(name = "ListGhostsRequest", namespace = Ghost.NAMESPACE)
class ListGhostsRequest(
    @field:XmlElement(nillable = true)
    val includeHouses: Boolean = false
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsResponse", propOrder = ["ghosts"])
@XmlRootElement(name = "ListGhostsResponse", namespace = Ghost.NAMESPACE)
data class ListGhostsResponse(
    @field:XmlElement(name = "ghost")
    val ghosts: List<Ghost> = emptyList()
)