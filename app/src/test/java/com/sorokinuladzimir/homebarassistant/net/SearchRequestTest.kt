package com.sorokinuladzimir.homebarassistant.net

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchRequestTest {

    @Test
    fun `json round trip preserves queries`() {
        val request = SearchRequest(
            listOf(
                SearchConditions(name = "Green"),
                SearchConditions(alcoholic = true)
            )
        )

        assertEquals(request, SearchRequest.fromJson(request.toJson()))
    }

    @Test
    fun `single wraps one condition`() {
        assertEquals(
            listOf(SearchConditions(ingredient = "Gin")),
            SearchRequest.single(SearchConditions(ingredient = "Gin")).queries
        )
    }

    @Test
    fun `blank or malformed input yields empty request`() {
        assertEquals(emptyList<SearchConditions>(), SearchRequest.fromJson(null).queries)
        assertEquals(emptyList<SearchConditions>(), SearchRequest.fromJson("").queries)
        assertEquals(emptyList<SearchConditions>(), SearchRequest.fromJson("not json").queries)
    }
}
