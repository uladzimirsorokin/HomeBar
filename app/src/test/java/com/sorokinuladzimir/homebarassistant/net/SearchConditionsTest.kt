package com.sorokinuladzimir.homebarassistant.net

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchConditionsTest {

    @Test
    fun `json round trip preserves supported and ignored fields`() {
        val conditions = SearchConditions(
            name = "margarita",
            ingredient = "Gin",
            glass = "Cocktail_glass",
            alcoholic = true,
            taste = "sweet",
            carbonated = true,
            color = "red",
            skill = 2
        )

        assertEquals(conditions, SearchConditions.fromJson(conditions.toJson()))
    }

    @Test
    fun `blank input yields empty conditions`() {
        assertEquals(SearchConditions(), SearchConditions.fromJson(null))
        assertEquals(SearchConditions(), SearchConditions.fromJson(""))
        assertEquals(SearchConditions(), SearchConditions.fromJson("   "))
    }

    @Test
    fun `malformed input yields empty conditions`() {
        assertEquals(SearchConditions(), SearchConditions.fromJson("not json"))
    }
}
