package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CocktailDbResponse(
    @SerialName("drinks") val drinks: List<CocktailDbDrink>? = null
)
