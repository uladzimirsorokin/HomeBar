package com.sorokinuladzimir.homebarassistant.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.sorokinuladzimir.homebarassistant.db.converter.TasteConverter
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(TasteConverter::class)
interface DrinkDao {

    @Query("SELECT * FROM Drink WHERE id = :id")
    fun loadDrinkById(id: Long?): Flow<Drink?>

    @Query("SELECT * FROM Drink ORDER BY name ASC")
    fun loadAllDrinks(): Flow<List<Drink>>

    @Query("SELECT * FROM Drink WHERE name LIKE :query ORDER BY name ASC")
    fun searchDrinksByName(query: String): Flow<List<Drink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: Drink): Long

    @Query("delete from Drink where id = :drinkId")
    suspend fun deleteDrinkById(drinkId: Long)
}
