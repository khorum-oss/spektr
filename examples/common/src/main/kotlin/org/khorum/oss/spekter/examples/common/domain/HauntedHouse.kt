package org.khorum.oss.spekter.examples.common.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElements
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

    @field:XmlElements(
        XmlElement(name = "usAddress", type = UsAddress::class),
        XmlElement(name = "caAddress", type = CaAddress::class),
        XmlElement(name = "address", type = GenericAddress::class)
    )
    val address: Address = GenericAddress(
        streetLine1 = "", city = "", postalCode = "", country = ""
    ),

    @field:XmlTransient
    @JsonIgnore
    var ghostReports: Map<Ghost, GhostReport>? = null
) {
    /** JSON-friendly representation of ghosts for REST API responses */
    @JsonGetter("ghosts")
    fun getGhostsForJson(): List<Ghost>? = ghostReports?.keys?.toList()
}

/** A unique type of ghost */
typealias GhostType = String

data class CreateHauntedHouseRequest(
    val address: Address? = null,
    val ghosts: List<GhostType>? = null
)