package com.sorokinuladzimir.homebarassistant.net

import com.sorokinuladzimir.homebarassistant.net.entity.CocktailDbResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TheCocktailDB API (https://www.thecocktaildb.com/documentation).
 * The API key is part of the base URL (the free test key is `1`).
 */
interface CocktailDbApi {

    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): CocktailDbResponse

    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): CocktailDbResponse

    @GET("lookup.php")
    suspend fun lookup(@Query("i") id: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun filterByGlass(@Query("g") glass: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun filterByAlcoholic(@Query("a") alcoholic: String): CocktailDbResponse
}
