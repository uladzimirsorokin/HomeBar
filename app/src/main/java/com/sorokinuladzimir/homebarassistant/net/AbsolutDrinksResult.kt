package com.sorokinuladzimir.homebarassistant.net

import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AbsolutDrinksResult(
    @SerialName("result") val result: List<DrinkEntity>? = null,
    @SerialName("totalResult") val totalResult: Int = 0,
    @SerialName("next") val next: String? = null
)
