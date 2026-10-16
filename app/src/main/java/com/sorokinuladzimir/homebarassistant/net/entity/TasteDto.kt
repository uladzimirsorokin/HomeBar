package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class TasteDto(
    val id: String? = null,
    val text: String? = null
) : JavaSerializable
