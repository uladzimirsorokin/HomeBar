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
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;


@Dao
@TypeConverters(TasteConverter.class)
public interface DrinkDao {

    @Query("SELECT * FROM Drink WHERE id = :id")
    LiveData<Drink> loadDrinkById(int id);

    @Query("SELECT * FROM Drink")
    LiveData<List<Drink>> loadAllDrinks();

    @Insert(onConflict = IGNORE)
    void insertDrink(Drink drink);

    @Update(onConflict = REPLACE)
    void updateDrink(Drink book);

    @Delete
    void delete(Drink drink);

    @Query("DELETE FROM Drink")
    void deleteAll();
}
