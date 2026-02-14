package org.khorum.oss.spekter.examples.common.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlSeeAlso
import jakarta.xml.bind.annotation.XmlType

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "country"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = UsAddress::class, name = "US"),
    JsonSubTypes.Type(value = CaAddress::class, name = "CA"),
    JsonSubTypes.Type(value = GenericAddress::class, name = "OTHER")
)
@XmlSeeAlso(UsAddress::class, CaAddress::class, GenericAddress::class)
sealed interface Address {
    val streetLine1: String
    val streetLine2: String?
    val city: String
    val postalCode: String
    val country: String

    fun stateOrProvince(): String?

    companion object {
        fun create(
            streetLine1: String,
            streetLine2: String? = null,
            city: String,
            stateOrProvince: String? = null,
            postalCode: String,
            country: String
        ): Address = when (country.uppercase()) {
            "US" -> UsAddress(streetLine1, streetLine2, city, stateOrProvince ?: "", postalCode)
            "CA" -> CaAddress(streetLine1, streetLine2, city, stateOrProvince ?: "", postalCode)
            else -> GenericAddress(streetLine1, streetLine2, city, stateOrProvince, postalCode, country)
        }
    }
}

@XmlRootElement(name = "usAddress")
@XmlType(name = "UsAddressType")
data class UsAddress(
    @get:XmlElement(required = true)
    override val streetLine1: String,
    @get:XmlElement
    override val streetLine2: String? = null,
    @get:XmlElement(required = true)
    override val city: String,
    @get:XmlElement(required = true)
    val state: String,
    @get:XmlElement(required = true)
    override val postalCode: String,
    @get:XmlElement(required = true)
    override val country: String = "US"
) : Address {
    override fun stateOrProvince(): String = state
}

@XmlRootElement(name = "caAddress")
@XmlType(name = "CaAddressType")
data class CaAddress(
    @get:XmlElement(required = true)
    override val streetLine1: String,
    @get:XmlElement
    override val streetLine2: String? = null,
    @get:XmlElement(required = true)
    override val city: String,
    @get:XmlElement(required = true)
    val province: String,
    @get:XmlElement(required = true)
    override val postalCode: String,
    @get:XmlElement(required = true)
    override val country: String = "CA"
) : Address {
    override fun stateOrProvince(): String = province
}

@XmlRootElement(name = "genericAddress")
@XmlType(name = "GenericAddressType")
data class GenericAddress(
    @get:XmlElement(required = true)
    override val streetLine1: String,
    @get:XmlElement
    override val streetLine2: String? = null,
    @get:XmlElement(required = true)
    override val city: String,
    @get:XmlElement(required = true)
    val region: String? = null,
    @get:XmlElement(required = true)
    override val postalCode: String,
    @get:XmlElement(required = true)
    override val country: String
) : Address {
    override fun stateOrProvince(): String? = region
}

data class CreateAddressRequest(
    val streetLine1: String? = null,
    val streetLine2: String? = null,
    val city: String? = null,
    val stateOrProvince: String? = null,
    val postalCode: String? = null,
    val country: String? = null
) {
    fun toAddress(): Address {
        return Address.create(
            streetLine1 = requireNotNull(streetLine1) { "streetLine1 is required" },
            streetLine2 = streetLine2,
            city = requireNotNull(city) { "city is required" },
            stateOrProvince = stateOrProvince,
            postalCode = requireNotNull(postalCode) { "postalCode is required" },
            country = requireNotNull(country) { "country is required" }
        )
    }
}