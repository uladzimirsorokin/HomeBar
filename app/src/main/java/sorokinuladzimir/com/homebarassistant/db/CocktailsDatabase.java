package sorokinuladzimir.com.homebarassistant.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;

import sorokinuladzimir.com.homebarassistant.AppExecutors;
import sorokinuladzimir.com.homebarassistant.db.dao.CocktailDao;
import sorokinuladzimir.com.homebarassistant.db.dao.DrinkDao;
import sorokinuladzimir.com.homebarassistant.db.dao.IngredientDao;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;


@Database(entities = {Drink.class, Ingredient.class, DrinkIngredientJoin.class}, version = 1)
public abstract class CocktailsDatabase extends RoomDatabase {

    private static CocktailsDatabase sInstance;

    public static final String DB_NAME = "cocktails_db";

    public abstract DrinkDao getDrinkDao();

    public abstract IngredientDao getIngredientDao();

    public abstract CocktailDao getCocktailDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static CocktailsDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (CocktailsDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static CocktailsDatabase buildDatabase(final Context appContext,
                                                   final AppExecutors executors) {
        return Room.databaseBuilder(appContext, CocktailsDatabase.class, DB_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {
                            final CocktailsDatabase database = CocktailsDatabase.getInstance(appContext, executors);
                                   database.setDatabaseCreated();
                        });
                    }
                }).build();
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DB_NAME).exists()) {
            setDatabaseCreated();
        }
    }

}
