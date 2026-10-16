package com.sorokinuladzimir.homebarassistant.net

import com.sorokinuladzimir.homebarassistant.net.entity.Preparation
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AbsolutDrinksApi {

    @GET("/drinks/{conditions}")
    suspend fun getAllMatchedDrinks(
        @Path("conditions") conditions: String,
        @Query("start") start: Int,
        @Query("pageSize") pageSize: Int
    ): AbsolutDrinksResult

    @GET("quickSearch/drinks/{searchString}")
    suspend fun searchDrinks(
        @Path("searchString") searchString: String,
        @Query("start") start: Int,
        @Query("pageSize") pageSize: Int
    ): AbsolutDrinksResult

    @GET("/drinks/{drinkId}/howtomix")
    suspend fun getPreparationSteps(@Path("drinkId") drinkId: String): Preparation
}
