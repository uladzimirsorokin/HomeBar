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
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

import ru.terrakok.cicerone.commands.Replace;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface IngredientDao {
    @Query("SELECT * FROM Ingredient")
    List<Ingredient> loadAllIngredients();

    @Query("SELECT * FROM Ingredient")
    LiveData<List<Ingredient>> loadIngredients();

    @Query("SELECT * FROM Ingredient where id = :id")
    Ingredient loadIngredientById(Long id);

    @Query("SELECT * FROM Ingredient where id = :id")
    LiveData<Ingredient> loadIngredient(Long id);

    @Insert(onConflict = REPLACE)
    void insertIngredient(Ingredient ingredient);

    @Insert(onConflict = REPLACE)
    Long insertOrReplaceIngredient(Ingredient ingredient);

    @Delete
    void deleteIngredient(Ingredient ingredient);

    @Insert(onConflict = IGNORE)
    void insertOrReplaceIngredient(Ingredient... ingredient);

    @Insert(onConflict = REPLACE)
    Long[] insertOrReplaceIngredient(List<Ingredient> ingredients);

    @Delete
    void deleteIngredients(Ingredient ingredient1, Ingredient ingredient2);

    @Query("DELETE FROM Ingredient")
    void deleteAll();
}