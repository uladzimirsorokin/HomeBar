package com.sorokinuladzimir.homebarassistant.db.mapper

import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.Glass
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity

class DrinkEntityToDrinkMapper private constructor() : Mapper<Drink, DrinkEntity>() {

    override fun map(value: Drink): DrinkEntity = throw UnsupportedOperationException()

    override fun reverseMap(value: DrinkEntity): Drink {
        val drink = Drink()
        drink.name = value.name
        drink.tastes = ArrayList(value.tastes ?: arrayListOf())
        drink.description = value.description
        drink.image = Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + value.id + ".png"
        drink.rating = value.rating
        drink.isAlcoholic = value.isAlcoholic
        drink.isCarbonated = value.isCarbonated
        val glass = Glass()
        glass.glassName = value.servedIn?.text
        drink.glass = glass
        return drink
    }

    companion object {
        private val sInstance = DrinkEntityToDrinkMapper()

        @JvmStatic
        fun getInstance(): DrinkEntityToDrinkMapper = sInstance
    }
}
