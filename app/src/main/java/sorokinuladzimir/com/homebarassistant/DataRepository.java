package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;


/**
 * Repository handling the work with products and comments.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final CocktailsDatabase mDatabase;
    private MediatorLiveData<List<Drink>> mObservableDrinks;
    private AppExecutors mExecutors;

    private DataRepository(final CocktailsDatabase database, AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;
        mObservableDrinks = new MediatorLiveData<>();

        mObservableDrinks.addSource(mDatabase.getDrinkDao().loadAllDrinks(),
                new Observer<List<Drink>>() {
                    @Override
                    public void onChanged(@Nullable List<Drink> drinks) {
                        if (mDatabase.getDatabaseCreated().getValue() != null) {
                            mObservableDrinks.postValue(drinks);
                        }
                    }
                });
    }

    public static DataRepository getInstance(final CocktailsDatabase database,AppExecutors executors) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executors);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    public LiveData<List<Drink>> getDrinks() {
        return mObservableDrinks;
    }

    public LiveData<Drink> loadDrink(final int drinkId) {
        return mDatabase.getDrinkDao().loadDrinkById(drinkId);
    }

    public List<Ingredient> loadIngredients(){
        return mDatabase.getIngredientDao().loadAllIngredients();
    }

    public void insertDrink(final Drink drink) {

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.getDrinkDao().insertDrink(drink);
            }
        });

    }

    public LiveData<List<WholeCocktail>> loadIngredients(final int drinkId) {
        return mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId);
    }
}
