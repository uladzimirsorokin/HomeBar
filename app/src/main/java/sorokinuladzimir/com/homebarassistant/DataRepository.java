package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.ArrayList;
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
    private MediatorLiveData<List<Ingredient>> mObservableIngredients;
/*    private MediatorLiveData<Drink> mObservableDrink;
    private MediatorLiveData<List<WholeCocktail>> mObservableIngredients;*/


    private AppExecutors mExecutors;

    private DataRepository(final CocktailsDatabase database, AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mObservableDrinks = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();

        mObservableDrinks.addSource(mDatabase.getDrinkDao().loadAllDrinks(),
                drinks -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableDrinks.postValue(drinks);
                    }
                });

        mObservableIngredients.addSource(mDatabase.getIngredientDao().loadIngredients(),
        ingredients -> {
            if (mDatabase.getDatabaseCreated().getValue() != null) {
                mObservableIngredients.postValue(ingredients);
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

    public LiveData<List<Ingredient>> getIngredients() {
        return mObservableIngredients;
    }

    public List<WholeCocktail> getCustomIngredients(Long drinkId) {
        FutureTask<List<WholeCocktail>> future =
                new FutureTask<>(() -> mDatabase.getCocktailDao().getWholeCocktailIngr(drinkId));
        mExecutors.diskIO().execute(future);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public LiveData<Drink> loadDrink(final Long drinkId) {
        return mDatabase.getDrinkDao().loadDrinkById(drinkId);
    }

    public LiveData<Ingredient> loadIngredient(final Long ingredientId) {
        return mDatabase.getIngredientDao().loadIngredient(ingredientId);
    }

    public Drink getCustomDrink(final Long drinkId) {

        FutureTask<Drink> future =
                new FutureTask<>(() -> mDatabase.getDrinkDao().getDrink(drinkId));
        mExecutors.diskIO().execute(future);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Drink();
    }

    public List<Ingredient> loadIngredients(){

        FutureTask<List<Ingredient>> future =
                new FutureTask<>(() -> mDatabase.getIngredientDao().loadAllIngredients());
        mExecutors.diskIO().execute(future);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
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

    public Long insertIngredient(final Ingredient ingredient) {
        Long id = -1L;
        FutureTask<Long> future =
                new FutureTask<>(() -> mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient));
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
                new FutureTask<>(() -> mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredients));
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



    public LiveData<List<WholeCocktail>> loadIngredients(final Long drinkId) {
        return mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId);
    }

}
