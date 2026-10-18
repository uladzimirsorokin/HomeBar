package com.sorokinuladzimir.homebarassistant.db.mapper

import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import org.junit.Assert.assertEquals
import org.junit.Test

class WholeCocktailToDrinkIngredientJoinMapperTest {

    private val mapper = WholeCocktailToDrinkIngredientJoinMapper.getInstance()

    @Test
    fun `reverseMap copies id amount and unit`() {
        val cocktail = WholeCocktail().apply {
            jointableId = 7L
            ingredientId = 3L
            amount = "50"
            unit = "ml"
        }

        val join = mapper.reverseMap(cocktail)

        assertEquals(7L, join.id)
        assertEquals(3L, join.ingredientId)
        assertEquals("50", join.amount)
        assertEquals("ml", join.unit)
    }
}
