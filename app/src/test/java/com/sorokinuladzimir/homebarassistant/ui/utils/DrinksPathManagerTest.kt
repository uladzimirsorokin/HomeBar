package com.sorokinuladzimir.homebarassistant.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DrinksPathManagerTest {

    @Test
    fun `builds empty path when no conditions`() {
        val path = DrinksPathManager.Builder().build().path

        assertEquals("", path)
    }

    @Test
    fun `prefixes glass taste and ingredient`() {
        val path = DrinksPathManager.Builder()
            .setGlassType("highball-glass")
            .setTaste(arrayOf("sweet"))
            .setIngredient(arrayOf("vodka"))
            .setColor("red")
            .setRating(0, 100)
            .setSkill(arrayOf("1", "2"))
            .setCarbonated(true)
            .setAlcoholic(false)
            .build()
            .path

        val segments = path.split("/").filter { it.isNotEmpty() }
        assertEquals(listOf("servedin", "highball-glass"), segments.subList(0, 2))
        assert(path.contains("colored/red"))
        assert(path.contains("tasting/sweet"))
        assert(path.contains("withtype/vodka"))
        assert(path.contains("rating/gte0/and/lte100"))
        assert(path.contains("skill/1/or/2"))
        assert(path.contains("carbonated"))
        assert(path.contains("not/alcoholic"))
    }

    @Test
    fun `null optional conditions are skipped`() {
        val path = DrinksPathManager.Builder()
            .setGlassType(null)
            .setTaste(null)
            .setIngredient(null)
            .setColor(null)
            .build()
            .path

        assertEquals("", path)
    }
}
