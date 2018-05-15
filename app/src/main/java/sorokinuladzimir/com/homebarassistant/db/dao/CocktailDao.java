/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorokinuladzimir.com.homebarassistant.db.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
@TypeConverters(TasteConverter.class)
public interface CocktailDao {

    @Insert(onConflict = REPLACE)
    void insertDrinkIngredients(List<DrinkIngredientJoin> drinkIngredient);

    @Query("delete from DrinkIngredientJoin where drink_id = :drinkId")
    void deleteDrinkIngredientsById(Long drinkId);

    @Query("SELECT COUNT(*) from DrinkIngredientJoin where ingredient_id = :id")
    int countCocktailsWithIngridient(Long id);

    @Query("SELECT DrinkIngredientJoin.id as join_id, DrinkIngredientJoin.ingredient_id as id, Ingredient.name as ingredient, " +
            "DrinkIngredientJoin.amount, DrinkIngredientJoin.unit, Ingredient.image as image " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "WHERE drink_id Like :drinkID")
    LiveData<List<WholeCocktail>> findAllIngredientsByDrinkId(Long drinkID);

    @Query("SELECT * " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "INNER JOIN Drink ON DrinkIngredientJoin.drink_id = Drink.id " +
            "WHERE ingredient_id Like :ingredientId")
    LiveData<List<Drink>> getDrinksWithIngredient(Long ingredientId);
}
