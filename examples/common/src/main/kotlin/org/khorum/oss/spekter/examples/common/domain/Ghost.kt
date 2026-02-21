package org.khorum.oss.spekter.examples.common.domain

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElements
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ghost", propOrder = ["type", "origin", "houses"])
data class Ghost(
    @field:XmlElement(required = true)
    val type: String = "",
    @field:XmlElement(nillable = true)
    val origin: String? = null,
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "houses", nillable = true)
    var houses: List<HauntedHouse>? = null
) {
    companion object {
        const val NAMESPACE = "http://org.khorum-oss.com/ghost-book"
        const val PACKAGE = "org.khorum.oss.spekter.examples.common.domain"
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostRequest", propOrder = ["type", "origin", "addresses"])
@XmlRootElement(name = "createGhostRequest", namespace = Ghost.NAMESPACE)
data class CreateGhostRequest(
    @field:XmlElement(nillable = true)
    val type: String? = null,
    @field:XmlElement(nillable = true)
    val origin: String? = null,
    @field:XmlElementWrapper(name = "addresses", nillable = true)
    @field:XmlElements(
        XmlElement(name = "usAddress", type = UsAddress::class),
        XmlElement(name = "caAddress", type = CaAddress::class),
        XmlElement(name = "address", type = GenericAddress::class)
    )
    val addresses: List<Address>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateGhostResponse", propOrder = ["ghost", "houses"])
@XmlRootElement(name = "createGhostResponse", namespace = Ghost.NAMESPACE)
data class CreateGhostResponse(
    @field:XmlElement(required = true)
    val ghost: Ghost = Ghost(""),
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "houses", nillable = true)
    val houses: List<HauntedHouse>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostRequest", propOrder = ["type", "includeHouses"])
@XmlRootElement(name = "getGhostRequest", namespace = Ghost.NAMESPACE)
data class GetGhostRequest(
    @field:XmlElement(nillable = true)
    val type: String = "",
    @field:XmlElement(nillable = true)
    val includeHouses: Boolean = false
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetGhostResponse", propOrder = ["ghost", "houses"])
@XmlRootElement(name = "getGhostResponse", namespace = Ghost.NAMESPACE)
data class GetGhostResponse(
    @field:XmlElement(required = false)
    val ghost: Ghost? = null,
    @field:XmlElement(nillable = true)
    @field:XmlElementWrapper(name = "houses", nillable = true)
    val houses: List<HauntedHouse>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsRequest", propOrder = ["includeHouses"])
@XmlRootElement(name = "listGhostsRequest", namespace = Ghost.NAMESPACE)
class ListGhostsRequest(
    @field:XmlElement(nillable = true)
    val includeHouses: Boolean = false
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListGhostsResponse", propOrder = ["ghosts"])
@XmlRootElement(name = "listGhostsResponse", namespace = Ghost.NAMESPACE)
data class ListGhostsResponse(
    @field:XmlElement(name = "ghost")
    val ghosts: List<Ghost> = emptyList()
)