package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class IngredientDto(
    @SerialName("name") val name: String? = null,
    @SerialName("measure") val measure: String? = null
)
