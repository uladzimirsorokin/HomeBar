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

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;


@Dao
@TypeConverters(TasteConverter.class)
public interface DrinkDao {

    @Query("SELECT * FROM Drink WHERE id = :id")
    LiveData<Drink> loadDrinkById(Long id);

    @Query("SELECT * FROM Drink ORDER BY name ASC")
    LiveData<List<Drink>> loadAllDrinks();

    @Query("SELECT * FROM Drink WHERE name LIKE :query ORDER BY name ASC")
    LiveData<List<Drink>> searchDrinksByName(String query);

    @Insert(onConflict = REPLACE)
    Long insertDrink(Drink drink);

    @Query("delete from Drink where id = :drinkId")
    void deleteDrinkById(Long drinkId);

}
