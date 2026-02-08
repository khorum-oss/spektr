package org.khorum.oss.spekter.examples.common

data class UsAddress(
    override val streetLine1: String,
    override val streetLine2: String,
    override val city: String,
    private var state: String,
    override val postalCode: String,
    override val country: String = "US"
) : Address {
    override fun stateOrProvince(): String = state
}

data class CreateUsAddressRequest(
    val streetLine1: String? = null,
    val streetLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null
)

interface Address {
    val streetLine1: String
    val streetLine2: String
    val city: String
    val postalCode: String
    val country: String

    fun stateOrProvince(): String
}