package com.sorokinuladzimir.homebarassistant.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Drink::class,
            parentColumns = ["id"],
            childColumns = ["drink_id"]
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"]
        )
    ],
    indices = [Index(value = ["drink_id"]), Index(value = ["ingredient_id"])]
)
class DrinkIngredientJoin {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var amount: String? = null

    var unit: String? = null

    @ColumnInfo(name = "drink_id")
    var drinkId: Long? = null

    @ColumnInfo(name = "ingredient_id")
    var ingredientId: Long? = null
}
