package com.sorokinuladzimir.homebarassistant.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sorokinuladzimir.homebarassistant.db.entity.Taste

class TasteConverter {

    companion object {

        @TypeConverter
        @JvmStatic
        fun toList(value: String?): ArrayList<Taste>? {
            val listType = object : TypeToken<ArrayList<Taste>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromList(list: ArrayList<Taste>?): String? = Gson().toJson(list)
    }
}
