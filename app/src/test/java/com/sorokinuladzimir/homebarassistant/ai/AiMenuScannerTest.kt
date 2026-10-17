package com.sorokinuladzimir.homebarassistant.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiMenuScannerTest {

    private fun scan(json: String) =
        AiJson.decode(json, AiScannedDrink.serializer()).toCocktail()

    @Test
    fun `parses full menu result`() {
        val cocktail = scan(
            """{"confidence":0.9,"name":"Green Goblin","alcoholic":true,"glass":"Collins glass",
                "instructions":"Shake with ice.","ingredients":[
                {"name":"Vodka","measure":"1 oz"},{"name":"Melon liqueur","measure":null}]}"""
        )

        assertEquals("Green Goblin", cocktail.name)
        assertEquals(true, cocktail.alcoholic)
        assertEquals("Collins glass", cocktail.glass)
        assertEquals("Shake with ice.", cocktail.instructions)
        assertEquals(2, cocktail.ingredients.size)
        assertEquals("Vodka", cocktail.ingredients[0].name)
        assertEquals("1 oz", cocktail.ingredients[0].measure)
        assertNull(cocktail.ingredients[1].measure)
        assertTrue(cocktail.isGoodRecognition())
    }

    @Test
    fun `skips blank ingredient names`() {
        val cocktail = scan(
            """{"confidence":0.8,"name":"X","ingredients":[{"name":" "},{"name":"Gin"}]}"""
        )

        assertEquals(1, cocktail.ingredients.size)
        assertEquals("Gin", cocktail.ingredients[0].name)
    }

    @Test
    fun `confidence is clamped and drives good recognition`() {
        val low = scan("""{"confidence":0.2,"name":"X","ingredients":[{"name":"Gin"}]}""")
        assertFalse(low.isGoodRecognition())

        val noIngredients = scan("""{"confidence":0.9,"name":"X","ingredients":[]}""")
        assertFalse(noIngredients.isGoodRecognition())

        val noName = scan("""{"confidence":0.9,"ingredients":[{"name":"Gin"}]}""")
        assertFalse(noName.isGoodRecognition())
    }

    @Test
    fun `garbage image yields empty result`() {
        val cocktail = scan("""{"confidence":0,"name":null,"ingredients":null}""")

        assertNull(cocktail.name)
        assertTrue(cocktail.ingredients.isEmpty())
        assertFalse(cocktail.isGoodRecognition())
    }
}
