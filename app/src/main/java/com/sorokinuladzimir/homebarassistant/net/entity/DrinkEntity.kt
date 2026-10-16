package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class DrinkEntity(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("descriptionPlain") val description: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("rating") val rating: Int = 0,
    @SerialName("isAlcoholic") val isAlcoholic: Boolean = false,
    @SerialName("isCarbonated") val isCarbonated: Boolean = false,
    @SerialName("servedIn") val servedIn: Glass? = null,
    @SerialName("tastes") val tastes: List<TasteDto>? = null
) : JavaSerializable
