package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Process;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.ui.utils.ImageHandler;

import static android.support.v4.content.FileProvider.getUriForFile;

public class DataRepository {

    private static DataRepository sInstance;

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private final CocktailsDatabase mDatabase;

    private MediatorLiveData<List<Drink>> mObservableDrinks;

    private MediatorLiveData<List<Ingredient>> mObservableIngredients;

    private MediatorLiveData<String> mObservableIngredientImagePath;
    private MediatorLiveData<String> mObservableDrinkImagePath;

    private AppExecutors mExecutors;

    private ImageHandler imageHandler;

    private DataRepository(final CocktailsDatabase database, AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mObservableDrinks = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredientImagePath = new MediatorLiveData<>();
        mObservableDrinkImagePath = new MediatorLiveData<>();

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

        imageHandler = new ImageHandler();
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

    public LiveData<Drink> loadDrink(final Long drinkId) {
        return mDatabase.getDrinkDao().loadDrinkById(drinkId);
    }

    public LiveData<Ingredient> loadIngredient(final Long ingredientId) {
        return mDatabase.getIngredientDao().loadIngredient(ingredientId);
    }

    public LiveData<List<Ingredient>> loadIngredients(final List<Long> ingredientIds) {
        return mDatabase.getIngredientDao().loadIngredients(ingredientIds);
    }

    public LiveData<List<WholeCocktail>> loadCocktailIngredients(final List<Long> ingredientIds) {
        return mDatabase.getIngredientDao().loadCocktailIngredients(ingredientIds);
    }

    public LiveData<String> getObservableIngredientImagePath() {
        return mObservableIngredientImagePath;
    }

    public LiveData<String> getObservableDrinkImagePath() {
        return mObservableDrinkImagePath;
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

    public LiveData<List<Long>> loadIngredientIds(final Long drinkId) {
        return mDatabase.getCocktailDao().getDrinkIngredientIds(drinkId);
    }

    public Uri createImageFile(Context context, String albumName){
        File photoFile = null;

        try {
            photoFile = imageHandler.createImageFile(albumName);
        } catch (IOException ex) {

        }

        if (photoFile != null) {
            return getUriForFile(context,
                    AUTHORITY,
                    photoFile);
        }

        return null;
    }



    public void saveImageToAlbum(Context context,
                                 String albumName,
                                 Uri imageUri,
                                 int sizeForScale,
                                 boolean deleteSource,
                                 boolean ingredientImage) {

        mExecutors.diskIO().execute(()->{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                Bitmap bitmap = imageHandler.getBitmapFromUri(context, imageUri, sizeForScale);
                if (deleteSource) imageHandler.deleteImage(context, imageUri);
                if (ingredientImage) {
                    mObservableIngredientImagePath.postValue(imageHandler.saveImage(bitmap, albumName));
                } else {
                    mObservableDrinkImagePath.postValue(imageHandler.saveImage(bitmap, albumName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteDrink(Long drinkId) {
        mExecutors.diskIO().execute(() -> {
            mDatabase.getCocktailDao().deleteDrinkIngredientsById(drinkId);
            mDatabase.getDrinkDao().deleteDrinkById(drinkId);
        });
    }

    public void insertDrink(Drink drink, List<DrinkIngredientJoin> drinkIngredients) {
        mExecutors.diskIO().execute(() -> {
            if (drink.id != null) mDatabase.getCocktailDao().deleteDrinkIngredientsById(drink.id);
            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);
            for (final ListIterator<DrinkIngredientJoin> i = drinkIngredients.listIterator(); i.hasNext();) {
                final DrinkIngredientJoin item = i.next();
                item.drinkId = drinkId;
            }
            mDatabase.getCocktailDao().insertDrinkIngredients(drinkIngredients);
        });
    }

    public int deleteIngredient(Long ingredientId) {
        FutureTask<Integer> future =
                new FutureTask<>(() -> {
                        int count = mDatabase.getCocktailDao().countCocktailsWithIngridient(ingredientId);
                        if(count <= 0) mDatabase.getIngredientDao().deleteIngredientById(ingredientId);
                        return count;
                });
        mExecutors.diskIO().execute(future);

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteImage(Context context, String imagePath){
        mExecutors.diskIO().execute(()->{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Uri photoUri = FileProvider.getUriForFile(context,
                            AUTHORITY,
                            imageFile);
                    imageHandler.deleteImage(context, photoUri);
                }
            }
        });
    }

    //TODO: parse addb responce somehow to get amout/unit/ingredient separately
    public void saveDrinkFromNet(Drink drink, Bitmap bitmap, String albumName) {

        try {
            drink.image = imageHandler.saveImage(bitmap, albumName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long drinkId = insertDrink(drink);

        Long[] ingredientIds = insertIngredients(drink.ingredients);

        List<DrinkIngredientJoin> list = new ArrayList<>();
        for (Long ingredientId: ingredientIds) {
            DrinkIngredientJoin item = new DrinkIngredientJoin();
            item.ingredientId = ingredientId;
            item.drinkId = drinkId;
            item.amount = "" + list.size();
            item.unit = "Номер в списке " + list.size();
            list.add(item);
        }

        insertDrinkIngredientJoin(list);
    }

    public void resetIngredientImagePath() {
        mObservableIngredientImagePath.setValue(null);
    }

    public void resetDrinkImagePath() {
        mObservableDrinkImagePath.setValue(null);
    }
}
