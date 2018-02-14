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
    private MediatorLiveData<String> mObservableImagePath;


    private AppExecutors mExecutors;

    private ImageHandler imageHandler;

    private DataRepository(final CocktailsDatabase database, AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mObservableDrinks = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();
        mObservableImagePath = new MediatorLiveData<>();

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

    public LiveData<String> getObservableImagePath() {
        return mObservableImagePath;
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



    public void saveImageToAlbum(Context context, String albumName, Uri imageUri, int sizeForScale, boolean deleteSource) {

        mExecutors.diskIO().execute(()->{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                Bitmap bitmap = imageHandler.getBitmapFromUri(context, imageUri, sizeForScale);
                if (deleteSource) imageHandler.deleteImage(context, imageUri);
                mObservableImagePath.postValue(imageHandler.saveImage(bitmap, albumName));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            item.amount = 0L;
            item.unit = "Номер в списке " + list.size();
            list.add(item);
        }

        insertDrinkIngredientJoin(list);
    }
}
