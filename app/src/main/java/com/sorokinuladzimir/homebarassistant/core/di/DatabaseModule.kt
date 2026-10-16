package com.sorokinuladzimir.homebarassistant.core.di

import android.content.Context
import androidx.room.Room
import com.sorokinuladzimir.homebarassistant.db.CocktailsDatabase
import com.sorokinuladzimir.homebarassistant.db.dao.CocktailDao
import com.sorokinuladzimir.homebarassistant.db.dao.DrinkDao
import com.sorokinuladzimir.homebarassistant.db.dao.IngredientDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "cocktails_db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CocktailsDatabase =
        Room.databaseBuilder(context, CocktailsDatabase::class.java, DB_NAME).build()

    @Provides
    fun provideDrinkDao(database: CocktailsDatabase): DrinkDao = database.getDrinkDao()

    @Provides
    fun provideIngredientDao(database: CocktailsDatabase): IngredientDao = database.getIngredientDao()

    @Provides
    fun provideCocktailDao(database: CocktailsDatabase): CocktailDao = database.getCocktailDao()
}
