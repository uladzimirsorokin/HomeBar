package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Preparation(
    @SerialName("steps") val preparationSteps: List<PreparationStep>? = null
)
