package com.sorokinuladzimir.homebarassistant.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sorokinuladzimir.homebarassistant.db.converter.TasteConverter
import java.io.Serializable

@Entity(indices = [Index(value = ["id"], unique = true)])
@TypeConverters(TasteConverter::class)
class Drink : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var name: String? = null

    var description: String? = null

    var tastes: ArrayList<Taste>? = null

    var image: String? = null

    @Embedded
    var glass: Glass? = null

    var rating: Int = 0

    var isAlcoholic: Boolean = false

    var isCarbonated: Boolean = false

    var notes: String? = null
}
