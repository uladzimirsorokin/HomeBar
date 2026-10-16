package com.sorokinuladzimir.homebarassistant.net

import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import kotlinx.serialization.Serializable

@Serializable
class AbsolutDrinksResult(
    val result: List<DrinkEntity>? = null,
    val totalResult: Int = 0,
    val next: String? = null
)
