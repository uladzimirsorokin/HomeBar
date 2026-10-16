package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PreparationStep(
    @SerialName("description") val ingredientDescription: String? = null,
    val imagePath: String? = null,
    val imageName: String? = null,
    @SerialName("quantity") val amount: Float = 0f,
    @SerialName("centilitres") val centilitresAmount: Float = 0f,
    @SerialName("measurement") val unit: String? = null,
    @SerialName("ingredient") val ingredientName: String? = null,
    @SerialName("textPlain") val preparationStepText: String? = null
)
