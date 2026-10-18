package com.sorokinuladzimir.homebarassistant.db.entity

import androidx.room.ColumnInfo

class WholeCocktail {

    @ColumnInfo(name = "join_id")
    var jointableId: Long? = null

    @ColumnInfo(name = "id")
    var ingredientId: Long? = null

    @ColumnInfo(name = "ingredient")
    var ingredientName: String? = null

    var amount: String? = null

    var unit: String? = null

    var image: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as WholeCocktail
        return jointableId == that.jointableId &&
            ingredientId == that.ingredientId &&
            ingredientName == that.ingredientName &&
            amount == that.amount &&
            unit == that.unit &&
            image == that.image
    }

    override fun hashCode(): Int =
        listOf(jointableId, ingredientId, ingredientName, amount, unit, image).hashCode()
}
