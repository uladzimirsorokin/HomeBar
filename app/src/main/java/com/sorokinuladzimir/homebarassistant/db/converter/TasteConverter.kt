package com.sorokinuladzimir.homebarassistant.db.converter

import androidx.room.TypeConverter
import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TasteConverter {

    companion object {

        private val json = Json { ignoreUnknownKeys = true }

        @TypeConverter
        @JvmStatic
        fun toList(value: String?): ArrayList<Taste>? =
            value?.let { ArrayList(json.decodeFromString<List<Taste>>(it)) }

        @TypeConverter
        @JvmStatic
        fun fromList(list: ArrayList<Taste>?): String? =
            list?.let { json.encodeToString(it) }
    }
}
