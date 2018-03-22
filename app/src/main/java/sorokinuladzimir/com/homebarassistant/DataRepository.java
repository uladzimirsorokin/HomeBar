package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.db.mapper.RawIngredientToWholeCocktailMapper;
import sorokinuladzimir.com.homebarassistant.ui.utils.ImageHandler;
import sorokinuladzimir.com.homebarassistant.ui.utils.IngredientParcer;

public class DataRepository {

    private static DataRepository sInstance;
    private Context mContext;

    private final CocktailsDatabase mDatabase;

    private MediatorLiveData<List<Drink>> mObservableDrinks;

    private MediatorLiveData<List<Ingredient>> mObservableIngredients;

    private MediatorLiveData<String> mObservableIngredientImagePath;
    private MediatorLiveData<String> mObservableDrinkImagePath;

    private AppExecutors mExecutors;

    private ImageHandler imageHandler;

    private DataRepository(final CocktailsDatabase database, AppExecutors executors, Context context) {
        mDatabase = database;
        mExecutors = executors;
        mContext = context;

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

    public static DataRepository getInstance(final CocktailsDatabase database,AppExecutors executors, Context context) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executors, context);
                }
            }
        }
        return sInstance;
    }


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

    public LiveData<List<WholeCocktail>> loadCocktailIngredients(final List<Long> ingredientIds) {
        return mDatabase.getIngredientDao().loadCocktailIngredients(ingredientIds);
    }

    public LiveData<String> getObservableIngredientImagePath() {
        return mObservableIngredientImagePath;
    }

    public LiveData<String> getObservableDrinkImagePath() {
        return mObservableDrinkImagePath;
    }


    public void insertIngredient(final Ingredient ingredient) {
      mExecutors.diskIO().execute(()->{
          mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient);
      });
    }

    public void insertDrinkIngredientJoin(final List<DrinkIngredientJoin> items) {
        mExecutors.diskIO().execute(() -> mDatabase.getCocktailDao().insertDrinkIngredients(items));
    }

    public LiveData<List<WholeCocktail>> loadIngredients(final Long drinkId) {
        return mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId);
    }

    public Uri createImageFile(){
        return imageHandler.createImageFile(mContext);
    }

    public void saveImageToAlbum(Uri imageUri,
                                 int sizeForScale,
                                 boolean deleteSource,
                                 boolean ingredientImage) {

        mExecutors.diskIO().execute(()->{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                Bitmap bitmap = imageHandler.getBitmapFromUri(mContext, imageUri, sizeForScale);
                if (deleteSource) imageHandler.deleteImage(mContext, imageUri);
                if (ingredientImage) {
                    mObservableIngredientImagePath.postValue(imageHandler.saveImage(bitmap));
                } else {
                    mObservableDrinkImagePath.postValue(imageHandler.saveImage(bitmap));
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
            if (drink.getId() != null) mDatabase.getCocktailDao().deleteDrinkIngredientsById(drink.getId());
            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);
            for (final ListIterator<DrinkIngredientJoin> i = drinkIngredients.listIterator(); i.hasNext();) {
                final DrinkIngredientJoin item = i.next();
                item.setDrinkId(drinkId);
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

    public void deleteImage(String imagePath){
        mExecutors.diskIO().execute(()->{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Uri photoUri = FileProvider.getUriForFile(mContext,
                            Constants.Strings.AUTHORITY,
                            imageFile);
                    imageHandler.deleteImage(mContext, photoUri);
                }
            }
        });
    }

    //TODO: parse addb responce somehow to get amout/unit/ingredient separately
    public void saveDrinkFromNet(Drink drink, Bitmap bitmap) {
        mExecutors.diskIO().execute(()->{
            try {
                drink.setImage(imageHandler.saveImage(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<WholeCocktail> wholeCocktailList = RawIngredientToWholeCocktailMapper.getInstance().reverseMap(drink.getIngredients());

            for (Ingredient ingredient : drink.getIngredients()) {
                ingredient.setName(IngredientParcer.parseName(ingredient.getName()));
            }

            for (Ingredient ingredient : drink.getIngredients()) {
                ingredient.setName(IngredientParcer.parseName(ingredient.getName()));
            }

            Long[] ingredientIds = mDatabase.getIngredientDao().insertOrReplaceIngredient(drink.getIngredients());
            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);

            List<DrinkIngredientJoin> list = new ArrayList<>();
            for (int i = 0; i < ingredientIds.length; i++) {
                DrinkIngredientJoin item = new DrinkIngredientJoin();
                item.setIngredientId(ingredientIds[i]);
                item.setDrinkId(drinkId);
                item.setAmount(wholeCocktailList.get(i).getAmount());
                item.setUnit(wholeCocktailList.get(i).getUnit());
                list.add(item);
            }

            insertDrinkIngredientJoin(list);

        });
    }

    public void resetIngredientImagePath() {
        mObservableIngredientImagePath.setValue(null);
    }

    public void resetDrinkImagePath() {
        mObservableDrinkImagePath.setValue(null);
    }

    public LiveData<List<Drink>> searchDrinksByName(String query) {
        return mDatabase.getDrinkDao().searchDrinksByName('%'+query+'%');
    }

    public LiveData<List<Ingredient>> searchIngredients(String query) {
        return mDatabase.getIngredientDao().searchIngredientsByName('%'+query+'%');
    }
}
