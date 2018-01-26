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

import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

@Dao
@TypeConverters(TasteConverter.class)
public interface CocktailDao {

    @Query("SELECT * From DrinkIngredientJoin")
    List<DrinkIngredientJoin> findAllDrinkIngredients();

    @Query("SELECT * From DrinkIngredientJoin WHERE drink_id = :id")
    List<DrinkIngredientJoin> findAllDrinkIngredientsById(int id);

    @Query("SELECT * From DrinkIngredientJoin WHERE ingredient_id = :id")
    List<DrinkIngredientJoin> findAllDrinkIngredientsById2(int id);

    @Query("SELECT * From DrinkIngredientJoin WHERE amount >= :amount")
    List<DrinkIngredientJoin> findAllDrinkIngredientsByAmount(int amount);

    @Query("SELECT Ingredient.id, Ingredient.name, Ingredient.description, DrinkIngredientJoin.amount as amount, DrinkIngredientJoin.unit " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "INNER JOIN Drink ON DrinkIngredientJoin.drink_id = Drink.id " +
            "WHERE drink_id Like :drinkID")
    List<Ingredient> ingredientsForCocktail(int drinkID);

    @Query("SELECT DrinkIngredientJoin.id, Ingredient.name as ingredient, Drink.name as drink, DrinkIngredientJoin.amount, DrinkIngredientJoin.unit " +
            "FROM DrinkIngredientJoin " +
            "INNER JOIN Ingredient ON DrinkIngredientJoin.ingredient_id = Ingredient.id " +
            "INNER JOIN Drink ON DrinkIngredientJoin.drink_id = Drink.id " +
            "WHERE drink_id Like :drinkID")
    LiveData<List<WholeCocktail>> findAllIngredientsByDrinkId(int drinkID);

    @Insert()
    void insertDrinkIngredient(DrinkIngredientJoin drinkIngredient);

    @Insert()
    void insertDrinkIngredients(DrinkIngredientJoin... drinkIngredient);

    @Insert()
    void insertDrinkIngredients(List<DrinkIngredientJoin> drinkIngredient);

    @Query("DELETE FROM DrinkIngredientJoin")
    void deleteAll();
}
