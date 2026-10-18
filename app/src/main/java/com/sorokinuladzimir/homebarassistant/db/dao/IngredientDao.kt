package com.sorokinuladzimir.homebarassistant.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM Ingredient")
    fun loadIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM Ingredient WHERE name LIKE :query ORDER BY name ASC")
    fun searchIngredientsByName(query: String): Flow<List<Ingredient>>

    @Query(
        "SELECT Ingredient.id as id, Ingredient.name as ingredient " +
            "FROM Ingredient WHERE Ingredient.id IN (:ingredientIds)"
    )
    fun loadCocktailIngredients(ingredientIds: List<Long>): Flow<List<WholeCocktail>>

    @Query("SELECT * FROM Ingredient where name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String?): Ingredient?

    @Query("SELECT * FROM Ingredient where id = :id")
    fun loadIngredient(id: Long?): Flow<Ingredient?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceIngredient(ingredient: Ingredient): Long

    @Query("delete from Ingredient where id = :ingredientId")
    suspend fun deleteIngredientById(ingredientId: Long)
}
