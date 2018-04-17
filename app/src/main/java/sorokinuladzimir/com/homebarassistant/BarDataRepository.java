package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.db.mapper.DrinkEntityToDrinkMapper;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksApi;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksResult;
import sorokinuladzimir.com.homebarassistant.net.NoConnectivityException;
import sorokinuladzimir.com.homebarassistant.net.RetrofitInstance;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.entity.Preparation;
import sorokinuladzimir.com.homebarassistant.net.entity.PreparationStep;
import sorokinuladzimir.com.homebarassistant.ui.utils.ImageHandler;

public class BarDataRepository implements BarData {

    private ArrayList<Ingredient> mRemoteIngredients;

    public enum QueryType{
        SEARCH_BY_NAME, SEARCH_BY_CONDITIONS, CURRENT
    }

    private static BarDataRepository sInstance;
    private Context mContext;

    private final CocktailsDatabase mDatabase;

    private MediatorLiveData<List<Drink>> mObservableDrinks;

    private MediatorLiveData<List<Ingredient>> mObservableIngredients;

    private MediatorLiveData<String> mObservableIngredientImagePath;
    private MediatorLiveData<String> mObservableDrinkImagePath;

    private AppExecutors mExecutors;

    private ImageHandler imageHandler;

    private MediatorLiveData<List<DrinkEntity>> mObservableRemoteDrinks;

    private MediatorLiveData<List<WholeCocktail>> mRemoteDrinkIngredients;

    private int mTotalResult = 0;
    private String mNextLink;
    private QueryType mCurrentSearchType;

    private BarDataRepository(final CocktailsDatabase database, AppExecutors executors, Context context) {
        mDatabase = database;
        mExecutors = executors;
        mContext = context;

        mObservableDrinks = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredientImagePath = new MediatorLiveData<>();
        mObservableDrinkImagePath = new MediatorLiveData<>();
        mObservableRemoteDrinks = new MediatorLiveData<>();
        mRemoteDrinkIngredients = new MediatorLiveData<>();

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

    public static BarDataRepository getInstance(final CocktailsDatabase database,AppExecutors executors, Context context) {
        if (sInstance == null) {
            synchronized (BarDataRepository.class) {
                if (sInstance == null) {
                    sInstance = new BarDataRepository(database, executors, context);
                }
            }
        }
        return sInstance;
    }


    @Override
    public LiveData<List<Drink>> getLocalDrinks() {
        return mObservableDrinks;
    }

    @Override
    public LiveData<Drink> getDrink(Long drinkId) {
        return mDatabase.getDrinkDao().loadDrinkById(drinkId);
    }

    @Override
    public LiveData<List<Drink>> getDrinksByName(String searchQuery) {
        return mDatabase.getDrinkDao().searchDrinksByName('%'+searchQuery+'%');
    }

    @Override
    public LiveData<List<Drink>> getDrinksByIngredient(Long ingredientId) {
        return mDatabase.getCocktailDao().getDrinksWithIngredient(ingredientId);
    }

    @Override
    public void getRemoteDrinks(String requestConditions, QueryType queryType, boolean clearList) {

        mExecutors.networkIO().execute(() -> {
            if (clearList) clearRemoteDrinks();
            if (mCurrentSearchType == null && !queryType.equals(QueryType.CURRENT)) mCurrentSearchType = queryType;
            if (!mCurrentSearchType.equals(queryType) && !queryType.equals(QueryType.CURRENT)) mCurrentSearchType = queryType;

            int start = (mObservableRemoteDrinks.getValue() == null) ? 0 : mObservableRemoteDrinks.getValue().size();

            AbsolutDrinksApi client =  RetrofitInstance
                    .getRetrofitInstance(mContext, "ru")
                    .create(AbsolutDrinksApi.class);

            Call<AbsolutDrinksResult> call;
            switch (mCurrentSearchType) {
                case SEARCH_BY_CONDITIONS:
                    call = client.getAllMatchedDrinks(requestConditions, start, Constants.Values.DEFAULT_ITEM_AMOUNT);
                    break;
                case SEARCH_BY_NAME:
                    call = client.searchDrinks(requestConditions, start, Constants.Values.DEFAULT_ITEM_AMOUNT);
                    break;
                default:
                    call = null;
            }

            if (call != null) {
                if (!(queryType.equals(QueryType.CURRENT) && start >= mTotalResult)) {
                    loadDrinks(call);
                }
            } else {
                Toast.makeText(mContext, "something went wrong :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearRemoteDrinks(){
        if (mObservableRemoteDrinks.getValue() != null) mObservableRemoteDrinks.getValue().clear();
    }

    public LiveData<List<DrinkEntity>> getObservableRemoteDrinks() {
        return mObservableRemoteDrinks;
    }

    public LiveData<List<WholeCocktail>> getRemoteDrinkIngredients() {
        return mRemoteDrinkIngredients;
    }

    @Override
    public void saveRemoteDrink(DrinkEntity drinkEntity, Bitmap bitmap) {
        mExecutors.diskIO().execute(() -> {

            Drink drink = DrinkEntityToDrinkMapper.getInstance().reverseMap(drinkEntity);

            try {
                drink.setImage(imageHandler.saveImage(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);
            List<DrinkIngredientJoin> joinList = new ArrayList<>();
            List<Ingredient> ingredients = new ArrayList<>(mRemoteIngredients);
            List<WholeCocktail> wholeCocktailList = mRemoteDrinkIngredients.getValue();

            for (Ingredient ingredient : ingredients) {
                DrinkIngredientJoin drinkIngredientJoin = new DrinkIngredientJoin();
                drinkIngredientJoin.setDrinkId(drinkId);
                WholeCocktail wholeCocktail = wholeCocktailList != null ? wholeCocktailList.get(ingredients.indexOf(ingredient)) : null;
                if (wholeCocktail != null) {
                    drinkIngredientJoin.setAmount(wholeCocktail.getAmount());
                    drinkIngredientJoin.setUnit(wholeCocktail.getUnit());
                }

                Ingredient dbIngredient = mDatabase.getIngredientDao().getIngredientByName(ingredient.getName());
                Long ingredientId;
                if (dbIngredient != null) {
                    if (dbIngredient.getNotes() == null || dbIngredient.getNotes().equals("")) {
                        dbIngredient.setNotes(ingredient.getNotes());
                        ingredientId = mDatabase.getIngredientDao().insertOrReplaceIngredient(dbIngredient);
                    }   else {
                        ingredientId = dbIngredient.getId();
                    }
                } else {
                    ingredientId = mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient);
                }
                drinkIngredientJoin.setIngredientId(ingredientId);
                joinList.add(drinkIngredientJoin);
            }

            insertDrinkIngredientJoin(joinList);
        });
    }

    @Override
    public void getRemoteDrinkIngredients(String drinkId) {
        mExecutors.networkIO().execute(() -> {
            if (mRemoteDrinkIngredients.getValue() != null) mRemoteDrinkIngredients.getValue().clear();
            if (mRemoteIngredients != null) mRemoteIngredients.clear();

            AbsolutDrinksApi client =  RetrofitInstance
                    .getRetrofitInstance(mContext, "ru")
                    .create(AbsolutDrinksApi.class);

            Call<Preparation> call = client.getPreparationSteps(drinkId);

            call.enqueue(new Callback<Preparation>() {
                @Override
                public void onResponse(@NonNull Call<Preparation> call, @NonNull Response<Preparation> response) {
                    if (response.isSuccessful()) {
                        // The network call was a success and we got a response
                        Preparation preparation = response.body();
                        if (preparation != null) {

                            //StringBuilder detailedPreparation = new StringBuilder();
                            ArrayList<WholeCocktail> drinkIngredients = new ArrayList<>();
                            mRemoteIngredients = new ArrayList<>();

                            //int i = 1;
                            for (PreparationStep step : preparation.getPreparationSteps()) {
                                /*detailedPreparation.append(i++).append(") ")
                                        .append(step.getPreparationStepText())
                                        .append(System.getProperty("line.separator"));*/
                                WholeCocktail wholeCocktail = new WholeCocktail();
                                Ingredient ingredient = new Ingredient();

                                if (step.getAmount() != 0.0 || step.getCentilitresAmount() != 0.0) {
                                    Map<String, String> map = matchAmountAndUnit(step.getAmount(),
                                            step.getCentilitresAmount(), step.getUnit());
                                    wholeCocktail.setAmount(map.get("amount"));
                                    wholeCocktail.setUnit(map.get("unit"));
                                    wholeCocktail.setIngredientName(step.getIngredientName());
                                    wholeCocktail.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                                            step.getImagePath()+"/"+step.getImageName()+ ".png");
                                    drinkIngredients.add(wholeCocktail);

                                    ingredient.setName(step.getIngredientName());
                                    ingredient.setNotes(step.getIngredientDescription());
                                    ingredient.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                                            step.getImagePath()+"/"+step.getImageName()+ ".png");
                                    mRemoteIngredients.add(ingredient);
                                } else if (step.getImageName().equals("ice")) {
                                    wholeCocktail.setIngredientName(step.getIngredientName());
                                    wholeCocktail.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                                            step.getImagePath()+"/"+step.getImageName()+ ".png");
                                    drinkIngredients.add(wholeCocktail);

                                    ingredient.setName(step.getIngredientName());
                                    ingredient.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                                            step.getImagePath()+"/"+step.getImageName()+ ".png");
                                    ingredient.setNotes(step.getIngredientDescription());
                                    mRemoteIngredients.add(ingredient);
                                }
                            }

                            mRemoteDrinkIngredients.postValue(drinkIngredients);
                        }
                    } else {
                        Toast.makeText(mContext, "server returned error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Preparation> call, @NonNull Throwable t) {
                    // the network call was a failure
                    if (t instanceof NoConnectivityException) {
                        // No internet connection
                        Toast.makeText(mContext, "no internet", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private String formatFloatNumber(float number) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        return formatter.format(number);
    }

    private Map<String, String> matchAmountAndUnit(float amount, float centilitresAmount, String unit) {
        Map<String, String> result = new HashMap<>();
        if (amount != 0.0) {
            String[] unitPatterns = mContext.getResources().getStringArray(R.array.ingredient_unit_ru);
            String[] unitMatches = mContext.getResources().getStringArray(R.array.ingredient_unit_ru_matches);
            for (String unitPattern : unitPatterns) {
                if (unit != null && unit.toLowerCase().contains(unitPattern)) {
                    result.put("amount", formatFloatNumber(amount));
                    result.put("unit", unitMatches[Arrays.asList(unitPatterns).indexOf(unitPattern)]);
                    return result;
                }
            }
            result.put("amount", formatFloatNumber(amount));
            result.put("unit", "");
            return result;
        } else if (centilitresAmount != 0.0){
            result.put("amount", formatFloatNumber(centilitresAmount));
            result.put("unit", "cl");
            return result;
        }

        return result;
    }

    @Override
    public void saveDrink(Drink drink, List<DrinkIngredientJoin> drinkIngredients) {
        mExecutors.diskIO().execute(() -> {
            if (drink.getId() != null) mDatabase.getCocktailDao().deleteDrinkIngredientsById(drink.getId());
            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);
            for (final DrinkIngredientJoin item : drinkIngredients) {
                item.setDrinkId(drinkId);
            }
            mDatabase.getCocktailDao().insertDrinkIngredients(drinkIngredients);
        });
    }

    @Override
    public void deleteDrink(Long drinkId) {
        mExecutors.diskIO().execute(() -> {
            mDatabase.getCocktailDao().deleteDrinkIngredientsById(drinkId);
            mDatabase.getDrinkDao().deleteDrinkById(drinkId);
        });
    }

    @Override
    public LiveData<List<Ingredient>> getIngredients() {
        return mObservableIngredients;
    }

    @Override
    public LiveData<Ingredient> getIngredient(Long ingredientId) {
        return mDatabase.getIngredientDao().loadIngredient(ingredientId);
    }

    @Override
    public LiveData<List<Ingredient>> getIngredientsByName(String searchQuery) {
        return mDatabase.getIngredientDao().searchIngredientsByName('%'+searchQuery+'%');
    }

    @Override
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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public LiveData<List<WholeCocktail>> getDrinkIngredients(Long drinkId) {
        return mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId);
    }

    @Override
    public void addIngredient(Ingredient ingredient) {
        mExecutors.diskIO().execute(()-> mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient));
    }

    @Override
    public LiveData<List<WholeCocktail>> getListIngredientsNames(List<Long> ingredientIds) {
        return mDatabase.getIngredientDao().loadCocktailIngredients(ingredientIds);
    }

    @Override
    public void saveImageToAlbum(Uri imageUri, int sizeForScale, boolean deleteSource, boolean ingredientImage) {
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

    @Override
    public Uri createImageFile() {
        return imageHandler.createImageFile(mContext);
    }

    @Override
    public void deleteImage(String imagePath) {
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

    @Override
    public void resetIngredientImagePath() {
        mObservableIngredientImagePath.setValue(null);
    }

    @Override
    public void resetDrinkImagePath() {
        mObservableDrinkImagePath.setValue(null);
    }

    @Override
    public LiveData<String> getObservableDrinkImagePath() {
        return mObservableDrinkImagePath;
    }

    @Override
    public LiveData<String> getObservableIngredientImagePath() {
        return mObservableIngredientImagePath;
    }

    private void insertDrinkIngredientJoin(final List<DrinkIngredientJoin> items) {
        mExecutors.diskIO().execute(() -> mDatabase.getCocktailDao().insertDrinkIngredients(items));
    }

    private void loadDrinks(Call<AbsolutDrinksResult> call){

        call.enqueue(new Callback<AbsolutDrinksResult>() {
            @Override
            public void onResponse(@NonNull Call<AbsolutDrinksResult> call, @NonNull Response<AbsolutDrinksResult> response) {
                if (response.isSuccessful()) {
                    // The network call was a success and we got a response
                    setDrinksResult(response);
                } else {
                    Toast.makeText(mContext, "server returned error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AbsolutDrinksResult> call, @NonNull Throwable t) {
                // the network call was a failure
                if (t instanceof NoConnectivityException) {
                    // No internet connection
                    Toast.makeText(mContext, "no internet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setDrinksResult(Response<AbsolutDrinksResult> response){

        mTotalResult = Objects.requireNonNull(response.body()).getTotalResult();

        if (mObservableRemoteDrinks.getValue() != null && mNextLink != null
                && !mNextLink.equals(Objects.requireNonNull(response.body()).getNext())) {
            List<DrinkEntity> currentDrinksList = mObservableRemoteDrinks.getValue();
            currentDrinksList.addAll(Objects.requireNonNull(response.body()).getResult());
            mObservableRemoteDrinks.postValue(currentDrinksList);
        } else {
            mObservableRemoteDrinks.postValue(Objects.requireNonNull(response.body()).getResult());
        }

        mNextLink = Objects.requireNonNull(response.body()).getNext();
    }


}
