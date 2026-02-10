package org.khorum.oss.spekter.examples.common

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

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

data class UsAddress(
    override val streetLine1: String,
    override val streetLine2: String? = null,
    override val city: String,
    val state: String,
    override val postalCode: String,
    override val country: String = "US"
) : Address {
    override fun stateOrProvince(): String = state
}

data class CaAddress(
    override val streetLine1: String,
    override val streetLine2: String? = null,
    override val city: String,
    val province: String,
    override val postalCode: String,
    override val country: String = "CA"
) : Address {
    override fun stateOrProvince(): String = province
}

data class GenericAddress(
    override val streetLine1: String,
    override val streetLine2: String? = null,
    override val city: String,
    val region: String? = null,
    override val postalCode: String,
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