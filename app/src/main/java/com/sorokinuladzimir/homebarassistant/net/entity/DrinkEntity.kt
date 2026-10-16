package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class DrinkEntity(
    val id: String? = null,
    val name: String? = null,
    @SerialName("descriptionPlain") val description: String? = null,
    val color: String? = null,
    val rating: Int = 0,
    val isAlcoholic: Boolean = false,
    val isCarbonated: Boolean = false,
    val servedIn: Glass? = null,
    val tastes: List<TasteDto>? = null
) : JavaSerializable
