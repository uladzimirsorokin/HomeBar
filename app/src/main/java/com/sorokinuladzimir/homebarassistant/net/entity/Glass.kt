package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class Glass(
    @SerialName("text") val text: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("image") val image: Int = 0
) : JavaSerializable
