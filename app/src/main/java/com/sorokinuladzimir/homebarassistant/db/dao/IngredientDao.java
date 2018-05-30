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

package com.sorokinuladzimir.homebarassistant.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient;
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface IngredientDao {

    @Query("SELECT * FROM Ingredient")
    LiveData<List<Ingredient>> loadIngredients();

    @Query("SELECT * FROM Ingredient WHERE name LIKE :query ORDER BY name ASC")
    LiveData<List<Ingredient>> searchIngredientsByName(String query);

    @Query("SELECT Ingredient.id as id, Ingredient.name as ingredient FROM Ingredient WHERE Ingredient.id IN (:ingredientIds)")
    LiveData<List<WholeCocktail>> loadCocktailIngredients(List<Long> ingredientIds);

    @Query("SELECT * FROM Ingredient where name = :name LIMIT 1")
    Ingredient getIngredientByName(String name);

    @Query("SELECT * FROM Ingredient where id = :id")
    LiveData<Ingredient> loadIngredient(Long id);

    @Insert(onConflict = REPLACE)
    Long insertOrReplaceIngredient(Ingredient ingredient);

    @Query("delete from Ingredient where id = :ingredientId")
    void deleteIngredientById(Long ingredientId);

}