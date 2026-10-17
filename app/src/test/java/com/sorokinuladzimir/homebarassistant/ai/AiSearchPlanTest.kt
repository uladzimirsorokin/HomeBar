package com.sorokinuladzimir.homebarassistant.ai

import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiSearchPlanTest {

    private fun plan(json: String) = AiJson.decode(json, AiSearchPlan.serializer())

    @Test
    fun `ingredient plan maps to conditions`() {
        val conditions = plan("""{"ingredient":"Gin","reason":"gin request"}""").toConditions()

        assertEquals(SearchConditions(ingredient = "Gin"), conditions)
    }

    @Test
    fun `category spaces become underscores`() {
        val conditions = plan("""{"category":"Ordinary Drink"}""").toConditions()

        assertEquals(SearchConditions(category = "Ordinary_Drink"), conditions)
    }

    @Test
    fun `glass spaces become underscores`() {
        val conditions = plan("""{"glass":"Cocktail glass"}""").toConditions()

        assertEquals(SearchConditions(glass = "Cocktail_glass"), conditions)
    }

    @Test
    fun `alcoholic plan maps to boolean`() {
        val conditions = plan("""{"alcoholic":false}""").toConditions()

        assertEquals(SearchConditions(alcoholic = false), conditions)
    }

    @Test
    fun `name has priority when several fields are set`() {
        val conditions = plan(
            """{"name":"Fruity","ingredient":"Gin","alcoholic":true}"""
        ).toConditions()

        assertEquals(SearchConditions(name = "Fruity"), conditions)
    }

    @Test
    fun `empty plan yields null and callers fall back`() {
        assertNull(plan("""{"reason":"nothing"}""").toConditions())
    }

    @Test
    fun `markdown fences and prose are stripped`() {
        val conditions = plan(
            "Here is the plan:\n```json\n{\"ingredient\":\"Vodka\"}\n```\nDone."
        ).toConditions()

        assertEquals(SearchConditions(ingredient = "Vodka"), conditions)
    }
}
