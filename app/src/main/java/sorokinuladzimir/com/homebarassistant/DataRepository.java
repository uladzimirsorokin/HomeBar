package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
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

    public Long insertDrink(final Drink drink) {
        Long id = -1L;
        FutureTask<Long> future =
                new FutureTask<>(() -> mDatabase.getDrinkDao().insertDrink(drink));
        mExecutors.diskIO().execute(future);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void insertDrinkIngredientJoin(final List<DrinkIngredientJoin> items) {
        mExecutors.diskIO().execute(() -> mDatabase.getCocktailDao().insertDrinkIngredients(items));
    }

    public Long[] insertIngredients(final List<Ingredient> ingredients) {
        FutureTask<Long[]> future =
                new FutureTask<>(new Callable<Long[]>() {
                    public Long[] call() {
                        return mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredients);
                    }});
        mExecutors.diskIO().execute(future);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Long[]{};
    }



    public LiveData<List<WholeCocktail>> loadIngredients(final int drinkId) {
        return mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId);
    }
}
