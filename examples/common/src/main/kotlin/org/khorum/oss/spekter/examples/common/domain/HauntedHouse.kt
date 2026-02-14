package org.khorum.oss.spekter.examples.common.domain

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import jakarta.xml.bind.annotation.XmlType
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import org.khorum.oss.spekter.examples.common.UuidAdapter
import java.util.UUID

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HauntedHouseType", propOrder = ["id", "address"])
data class HauntedHouse(
    @field:XmlElement(required = true)
    @field:XmlJavaTypeAdapter(UuidAdapter::class)
    val id: UUID = UUID.randomUUID(),

    @field:XmlElement(required = true)
    val address: Address = GenericAddress(
        streetLine1 = "", city = "", postalCode = "", country = ""
    ),

    @field:XmlTransient
    val ghosts: Map<Ghost, GhostReport>? = null
)

/** A unique type of ghost */
typealias GhostType = String

data class CreateHauntedHouseRequest(
    val address: Address? = null,
    val ghosts: List<GhostType>? = null
)