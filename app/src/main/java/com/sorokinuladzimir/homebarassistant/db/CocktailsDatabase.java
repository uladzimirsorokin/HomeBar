package com.sorokinuladzimir.homebarassistant.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.sorokinuladzimir.homebarassistant.db.dao.CocktailDao;
import com.sorokinuladzimir.homebarassistant.db.dao.DrinkDao;
import com.sorokinuladzimir.homebarassistant.db.dao.IngredientDao;
import com.sorokinuladzimir.homebarassistant.db.entity.Drink;
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin;
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient;

import static com.sorokinuladzimir.homebarassistant.BarApp.getAppContext;


@Database(entities = {Drink.class, Ingredient.class, DrinkIngredientJoin.class}, version = 1,
        exportSchema = false)
public abstract class CocktailsDatabase extends RoomDatabase {

    private static final String DB_NAME = "cocktails_db";
    private static final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static CocktailsDatabase getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static CocktailsDatabase buildDatabase(final Context appContext) {
        Builder<CocktailsDatabase> build = Room.databaseBuilder(appContext, CocktailsDatabase.class, DB_NAME);
        updateDatabaseCreated(appContext);
        return build.build();
    }

    private static void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    public static LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    private static void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DB_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public abstract DrinkDao getDrinkDao();

    public abstract IngredientDao getIngredientDao();

    public abstract CocktailDao getCocktailDao();

    private static final class SingletonHolder {
        private static final CocktailsDatabase INSTANCE = buildDatabase(getAppContext());

        private SingletonHolder() {
        }
    }

}
