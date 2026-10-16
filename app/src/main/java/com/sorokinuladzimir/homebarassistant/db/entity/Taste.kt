package com.sorokinuladzimir.homebarassistant.db.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Taste(@SerialName("text") var text: String? = null) {

    @SerialName("id")
    var id: String? = null
}
