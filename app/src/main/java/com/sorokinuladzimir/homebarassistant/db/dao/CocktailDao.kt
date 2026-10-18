package com.sorokinuladzimir.homebarassistant.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.sorokinuladzimir.homebarassistant.db.converter.TasteConverter
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(TasteConverter::class)
interface CocktailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinkIngredients(drinkIngredient: List<DrinkIngredientJoin>)

    @Query("delete from DrinkIngredientJoin where drink_id = :drinkId")
    suspend fun deleteDrinkIngredientsById(drinkId: Long)

    @Query("SELECT COUNT(*) from DrinkIngredientJoin where ingredient_id = :id")
    suspend fun countCocktailsWithIngridient(id: Long): Int

    @Query(
        "SELECT DrinkIngredientJoin.id as join_id, DrinkIngredientJoin.ingredient_id as id, " +
            "Ingredient.name as ingredient, DrinkIngredientJoin.amount, DrinkIngredientJoin.unit, " +
            "Ingredient.image as image " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "WHERE drink_id Like :drinkID"
    )
    fun findAllIngredientsByDrinkId(drinkID: Long?): Flow<List<WholeCocktail>>

    @Query(
        "SELECT * " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "INNER JOIN Drink ON DrinkIngredientJoin.drink_id = Drink.id " +
            "WHERE ingredient_id Like :ingredientId"
    )
    fun getDrinksWithIngredient(ingredientId: Long?): Flow<List<Drink>>
}
