package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class TasteDto(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null
) : JavaSerializable
