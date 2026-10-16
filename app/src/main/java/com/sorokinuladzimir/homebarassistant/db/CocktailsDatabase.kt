package com.sorokinuladzimir.homebarassistant.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sorokinuladzimir.homebarassistant.BarApp
import com.sorokinuladzimir.homebarassistant.db.dao.CocktailDao
import com.sorokinuladzimir.homebarassistant.db.dao.DrinkDao
import com.sorokinuladzimir.homebarassistant.db.dao.IngredientDao
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient

@Database(
    entities = [Drink::class, Ingredient::class, DrinkIngredientJoin::class],
    version = 1,
    exportSchema = false
)
abstract class CocktailsDatabase : RoomDatabase() {

    abstract fun getDrinkDao(): DrinkDao

    abstract fun getIngredientDao(): IngredientDao

    abstract fun getCocktailDao(): CocktailDao

    companion object {

        private const val DB_NAME = "cocktails_db"
        private val mIsDatabaseCreated = MutableLiveData<Boolean>()

        private val INSTANCE: CocktailsDatabase by lazy { buildDatabase(BarApp.getAppContext()) }

        @JvmStatic
        fun getInstance(): CocktailsDatabase = INSTANCE

        @JvmStatic
        fun getDatabaseCreated(): LiveData<Boolean> = mIsDatabaseCreated

        private fun buildDatabase(appContext: Context): CocktailsDatabase {
            val build = Room.databaseBuilder(appContext, CocktailsDatabase::class.java, DB_NAME)
            updateDatabaseCreated(appContext)
            return build.build()
        }

        private fun setDatabaseCreated() {
            mIsDatabaseCreated.postValue(true)
        }

        private fun updateDatabaseCreated(context: Context) {
            if (context.getDatabasePath(DB_NAME).exists()) {
                setDatabaseCreated()
            }
        }
    }
}
