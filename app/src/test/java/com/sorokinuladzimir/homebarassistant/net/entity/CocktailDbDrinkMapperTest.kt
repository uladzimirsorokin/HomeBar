package com.sorokinuladzimir.homebarassistant.net.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CocktailDbDrinkMapperTest {

    @Test
    fun `maps full record`() {
        val dto = CocktailDbDrink(
            id = "11007",
            name = "Margarita",
            alcoholic = "Alcoholic",
            glass = "Cocktail glass",
            instructions = "Shake with ice",
            tags = "IBA,ContemporaryClassic",
            imageUrl = "https://example.com/margarita.jpg",
            ingredient1 = "Tequila",
            measure1 = "1 1/2 oz",
            ingredient2 = "Lime juice",
            measure2 = "1 oz"
        )

        val entity = CocktailDbDrinkMapper.map(dto)

        assertEquals("11007", entity.id)
        assertEquals("Margarita", entity.name)
        assertEquals("Shake with ice", entity.description)
        assertEquals("https://example.com/margarita.jpg", entity.imageUrl)
        assertEquals("Cocktail glass", entity.servedIn?.text)
        assertTrue(entity.isAlcoholic)
        assertFalse(entity.isCarbonated)
        assertEquals(listOf("IBA", "ContemporaryClassic"), entity.tastes?.map { it.text })
        assertEquals(2, entity.ingredients?.size)
        assertEquals("Tequila", entity.ingredients?.get(0)?.name)
        assertEquals("1 1/2 oz", entity.ingredients?.get(0)?.measure)
        assertEquals("Lime juice", entity.ingredients?.get(1)?.name)
    }

    @Test
    fun `maps non alcoholic and skips blank ingredients`() {
        val dto = CocktailDbDrink(
            id = "1",
            name = "Virgin",
            alcoholic = "Non_Alcoholic",
            ingredient1 = "Lime",
            ingredient2 = " ",
            measure1 = "1"
        )

        val entity = CocktailDbDrinkMapper.map(dto)

        assertFalse(entity.isAlcoholic)
        assertEquals(1, entity.ingredients?.size)
        assertNull(entity.servedIn)
    }

    @Test
    fun `drops spam and code-like tags`() {
        val dto = CocktailDbDrink(
            id = "2",
            name = "Spam",
            tags = "8rabyu,print(1589601882+2604639836);print(75+67),IBA,sweet"
        )

        val entity = CocktailDbDrinkMapper.map(dto)

        assertEquals(listOf("IBA", "sweet"), entity.tastes?.map { it.text })
    }

    @Test
    fun `blank tags yield no tastes`() {
        assertNull(CocktailDbDrinkMapper.map(CocktailDbDrink(tags = "  ")).tastes)
        assertNull(CocktailDbDrinkMapper.map(CocktailDbDrink(tags = null)).tastes)
    }
}
