package com.sorokinuladzimir.homebarassistant.db.mapper

import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail

class WholeCocktailToDrinkIngredientJoinMapper private constructor() :
    Mapper<DrinkIngredientJoin, WholeCocktail>() {

    override fun map(value: DrinkIngredientJoin): WholeCocktail =
        throw UnsupportedOperationException()

    override fun reverseMap(value: WholeCocktail): DrinkIngredientJoin {
        val drinkIngredient = DrinkIngredientJoin()
        drinkIngredient.id = value.jointableId
        drinkIngredient.ingredientId = value.ingredientId
        drinkIngredient.amount = value.amount
        drinkIngredient.unit = value.unit
        return drinkIngredient
    }

    companion object {
        private val sInstance = WholeCocktailToDrinkIngredientJoinMapper()

        @JvmStatic
        fun getInstance(): WholeCocktailToDrinkIngredientJoinMapper = sInstance
    }
}
