package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class Glass(
    val text: String? = null,
    val name: String? = null,
    val image: Int = 0
) : JavaSerializable
