package com.sorokinuladzimir.homebarassistant.ai

import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import com.sorokinuladzimir.homebarassistant.net.SearchRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class AiSearchPlanTest {

    private fun request(json: String): SearchRequest =
        AiJson.decode(json, AiSearchPlan.serializer()).toRequest()

    @Test
    fun `ingredient query maps to single condition`() {
        val queries = request("""{"queries":[{"ingredient":"Gin"}]}""").queries

        assertEquals(listOf(SearchConditions(ingredient = "Gin")), queries)
    }

    @Test
    fun `keeps multiple complementary queries in order`() {
        val queries = request(
            """{"queries":[{"name":"Green"},{"alcoholic":true}]}"""
        ).queries

        assertEquals(
            listOf(SearchConditions(name = "Green"), SearchConditions(alcoholic = true)),
            queries
        )
    }

    @Test
    fun `expands a query with several fields into separate conditions`() {
        val queries = request(
            """{"queries":[{"name":"Green","alcoholic":true}]}"""
        ).queries

        assertEquals(
            listOf(SearchConditions(name = "Green"), SearchConditions(alcoholic = true)),
            queries
        )
    }

    @Test
    fun `deduplicates and caps at three queries`() {
        val queries = request(
            """{"queries":[{"name":"Green"},{"name":"Green"},{"alcoholic":true},{"ingredient":"Gin"},{"glass":"Cocktail_glass"}]}"""
        ).queries

        assertEquals(3, queries.size)
        assertEquals(SearchConditions(name = "Green"), queries[0])
        assertEquals(SearchConditions(alcoholic = true), queries[1])
        assertEquals(SearchConditions(ingredient = "Gin"), queries[2])
    }

    @Test
    fun `category and glass spaces become underscores`() {
        val queries = request(
            """{"queries":[{"category":"Ordinary Drink"},{"glass":"Cocktail glass"}]}"""
        ).queries

        assertEquals(
            listOf(
                SearchConditions(category = "Ordinary_Drink"),
                SearchConditions(glass = "Cocktail_glass")
            ),
            queries
        )
    }

    @Test
    fun `legacy single-query shape is supported`() {
        val queries = request("""{"ingredient":"Vodka","reason":"legacy"}""").queries

        assertEquals(listOf(SearchConditions(ingredient = "Vodka")), queries)
    }

    @Test
    fun `empty plan yields no queries and callers fall back`() {
        assertEquals(emptyList<SearchConditions>(), request("""{"reason":"nothing"}""").queries)
    }

    @Test
    fun `markdown fences and prose are stripped`() {
        val queries = request(
            "Here is the plan:\n```json\n{\"queries\":[{\"ingredient\":\"Vodka\"}]}\n```\nDone."
        ).queries

        assertEquals(listOf(SearchConditions(ingredient = "Vodka")), queries)
    }
}
