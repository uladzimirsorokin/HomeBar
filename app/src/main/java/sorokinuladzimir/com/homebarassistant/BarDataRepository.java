package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
import sorokinuladzimir.com.homebarassistant.net.ServiceGenerator;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.entity.Preparation;
import sorokinuladzimir.com.homebarassistant.net.entity.PreparationStep;
import sorokinuladzimir.com.homebarassistant.ui.utils.ImageHandler;

public class BarDataRepository implements BarData {

    private final CocktailsDatabase mDatabase;
    private ArrayList<Ingredient> mRemoteIngredients;
    private final MediatorLiveData<List<Drink>> mObservableDrinks;
    private final MediatorLiveData<List<Ingredient>> mObservableIngredients;
    private MediatorLiveData<String> mObservableIngredientImagePath;
    private MediatorLiveData<String> mObservableDrinkImagePath;
    private AppExecutors mExecutors;
    private ImageHandler imageHandler;
    private MediatorLiveData<List<DrinkEntity>> mObservableRemoteDrinks;
    private MediatorLiveData<List<WholeCocktail>> mRemoteDrinkIngredients;
    private int mTotalResult = 0;
    private String mNextLink;
    private QueryType mCurrentSearchType;

    private BarDataRepository() {
        mDatabase = BarApp.getDatabase();
        mExecutors = BarApp.getExecutors();
        mObservableDrinks = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredientImagePath = new MediatorLiveData<>();
        mObservableDrinkImagePath = new MediatorLiveData<>();
        mObservableRemoteDrinks = new MediatorLiveData<>();
        mRemoteDrinkIngredients = new MediatorLiveData<>();
        mObservableDrinks.addSource(mDatabase.getDrinkDao().loadAllDrinks(),
                drinks -> {
                    if (CocktailsDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableDrinks.postValue(drinks);
                    }
                });
        mObservableIngredients.addSource(mDatabase.getIngredientDao().loadIngredients(),
                ingredients -> {
                    if (CocktailsDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableIngredients.postValue(ingredients);
                    }
                });
        imageHandler = new ImageHandler();
    }

    public static BarDataRepository getInstance() {
        return SingletonHolder.INSTANCE;
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
        return mDatabase.getDrinkDao().searchDrinksByName('%' + searchQuery + '%');
    }

    @Override
    public LiveData<List<Drink>> getDrinksByIngredient(Long ingredientId) {
        return mDatabase.getCocktailDao().getDrinksWithIngredient(ingredientId);
    }

    @Override
    public void getRemoteDrinks(String requestConditions, QueryType queryType, boolean clearList) {
        mExecutors.networkIO().execute(() -> {
            if (clearList) clearRemoteDrinks();
            int start = (mObservableRemoteDrinks.getValue() == null) ? 0 : mObservableRemoteDrinks.getValue().size();
            AbsolutDrinksApi client = ServiceGenerator.getInstance().getAbsolutDrinksService();
            Call<AbsolutDrinksResult> call = getDrinksCall(requestConditions, queryType, client, start);
            if (call != null) {
                if (!(queryType.equals(QueryType.CURRENT) && start >= mTotalResult)) {
                    loadDrinks(call);
                }
            } else {
                Toast.makeText(BarApp.getAppContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Call<AbsolutDrinksResult> getDrinksCall(String requestConditions, QueryType queryType, AbsolutDrinksApi client, int start) {
        if (mCurrentSearchType == null && !queryType.equals(QueryType.CURRENT))
            mCurrentSearchType = queryType;
        if (mCurrentSearchType != null) {
            if (!mCurrentSearchType.equals(queryType) && !queryType.equals(QueryType.CURRENT))
                mCurrentSearchType = queryType;
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
            return call;
        }
        return null;
    }

    private void clearRemoteDrinks() {
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
                //log this exception
            }
            Long drinkId = mDatabase.getDrinkDao().insertDrink(drink);
            List<Ingredient> ingredients = new ArrayList<>(mRemoteIngredients);
            List<WholeCocktail> wholeCocktailList = mRemoteDrinkIngredients.getValue();
            insertDrinkIngredientJoin(getDrinkIngredientsJoin(ingredients, wholeCocktailList, drinkId));
        });
    }

    private List<DrinkIngredientJoin> getDrinkIngredientsJoin(List<Ingredient> ingredients, List<WholeCocktail> wholeCocktailList,
                                                              Long drinkId) {
        List<DrinkIngredientJoin> joinList = new ArrayList<>();
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
                } else {
                    ingredientId = dbIngredient.getId();
                }
            } else {
                ingredientId = mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient);
            }
            drinkIngredientJoin.setIngredientId(ingredientId);
            joinList.add(drinkIngredientJoin);
        }
        return joinList;
    }

    @Override
    public void getRemoteDrinkIngredients(String drinkId) {
        mExecutors.networkIO().execute(() -> {
            if (mRemoteDrinkIngredients.getValue() != null)
                mRemoteDrinkIngredients.getValue().clear();
            if (mRemoteIngredients != null) mRemoteIngredients.clear();
            AbsolutDrinksApi client = ServiceGenerator.getInstance().getAbsolutDrinksService();
            Call<Preparation> call = client.getPreparationSteps(drinkId);
            call.enqueue(new Callback<Preparation>() {
                @Override
                public void onResponse(@NonNull Call<Preparation> call, @NonNull Response<Preparation> response) {
                    if (response.isSuccessful()) {
                        Preparation preparation = response.body();
                        handlePreparation(preparation);
                    } else {
                        Toast.makeText(BarApp.getAppContext(), "server returned error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Preparation> call, @NonNull Throwable t) {
                    // the network call was a failure
                    if (t instanceof NoConnectivityException) {
                        // No internet connection
                        Toast.makeText(BarApp.getAppContext(), "no internet", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BarApp.getAppContext(), "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void handlePreparation(Preparation preparation) {
        if (preparation != null) {
            ArrayList<WholeCocktail> drinkIngredients = new ArrayList<>();
            mRemoteIngredients = new ArrayList<>();
            for (PreparationStep step : preparation.getPreparationSteps()) {
                WholeCocktail wholeCocktail = new WholeCocktail();
                Ingredient ingredient = new Ingredient();
                if (step.getAmount() != 0.0 || step.getCentilitresAmount() != 0.0) {
                    Map<String, String> map = matchAmountAndUnit(step.getAmount(),
                            step.getCentilitresAmount(), step.getUnit());
                    wholeCocktail.setAmount(map.get(Constants.Extra.AMOUNT));
                    wholeCocktail.setUnit(map.get(Constants.Extra.UNIT));
                    wholeCocktail.setIngredientName(step.getIngredientName());
                    wholeCocktail.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                            step.getImagePath() + "/" + step.getImageName() + ".png");
                    drinkIngredients.add(wholeCocktail);
                    ingredient.setName(step.getIngredientName());
                    ingredient.setNotes(step.getIngredientDescription());
                    ingredient.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                            step.getImagePath() + "/" + step.getImageName() + ".png");
                    mRemoteIngredients.add(ingredient);
                } else if (step.getImageName().equals("ice")) {
                    wholeCocktail.setIngredientName(step.getIngredientName());
                    wholeCocktail.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                            step.getImagePath() + "/" + step.getImageName() + ".png");
                    drinkIngredients.add(wholeCocktail);
                    ingredient.setName(step.getIngredientName());
                    ingredient.setImage(Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT +
                            step.getImagePath() + "/" + step.getImageName() + ".png");
                    ingredient.setNotes(step.getIngredientDescription());
                    mRemoteIngredients.add(ingredient);
                }
            }
            mRemoteDrinkIngredients.postValue(drinkIngredients);
        }
    }

    private String formatFloatNumber(float number) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        return formatter.format(number);
    }

    private Map<String, String> matchAmountAndUnit(float amount, float centilitresAmount, String unit) {
        Map<String, String> result = new HashMap<>();
        if (amount != 0.0 && unit != null) {
            String[] unitPatterns = BarApp.getAppContext().getResources().getStringArray(R.array.ingredient_unit_ru);
            String[] unitMatches = BarApp.getAppContext().getResources().getStringArray(R.array.ingredient_unit_ru_matches);
            for (String unitPattern : unitPatterns) {
                if (unit.toLowerCase().contains(unitPattern)) {
                    result.put(Constants.Extra.AMOUNT, formatFloatNumber(amount));
                    result.put(Constants.Extra.UNIT, unitMatches[Arrays.asList(unitPatterns).indexOf(unitPattern)]);
                    return result;
                }
            }
            result.put(Constants.Extra.AMOUNT, formatFloatNumber(amount));
            result.put(Constants.Extra.UNIT, "");
            return result;
        } else if (centilitresAmount != 0.0) {
            result.put(Constants.Extra.AMOUNT, formatFloatNumber(centilitresAmount));
            result.put(Constants.Extra.UNIT, "cl");
            return result;
        }

        return result;
    }

    @Override
    public void saveDrink(Drink drink, List<DrinkIngredientJoin> drinkIngredients) {
        mExecutors.diskIO().execute(() -> {
            if (drink.getId() != null)
                mDatabase.getCocktailDao().deleteDrinkIngredientsById(drink.getId());
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
        return mDatabase.getIngredientDao().searchIngredientsByName('%' + searchQuery + '%');
    }

    @Override
    public int deleteIngredient(Long ingredientId) {
        FutureTask<Integer> future =
                new FutureTask<>(() -> {
                    int count = mDatabase.getCocktailDao().countCocktailsWithIngridient(ingredientId);
                    if (count <= 0) mDatabase.getIngredientDao().deleteIngredientById(ingredientId);
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
        mExecutors.diskIO().execute(() -> mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient));
    }

    @Override
    public LiveData<List<WholeCocktail>> getListIngredientsNames(List<Long> ingredientIds) {
        return mDatabase.getIngredientDao().loadCocktailIngredients(ingredientIds);
    }

    @Override
    public void saveImageToAlbum(Uri imageUri, int sizeForScale, boolean deleteSource, boolean ingredientImage) {
        mExecutors.diskIO().execute(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                Bitmap bitmap = imageHandler.getBitmapFromUri(BarApp.getAppContext(), imageUri, sizeForScale);
                if (deleteSource) imageHandler.deleteImage(BarApp.getAppContext(), imageUri);
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
        return imageHandler.createImageFile(BarApp.getAppContext());
    }

    @Override
    public void deleteImage(String imagePath) {
        mExecutors.diskIO().execute(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Uri photoUri = FileProvider.getUriForFile(BarApp.getAppContext(),
                            Constants.Strings.AUTHORITY,
                            imageFile);
                    imageHandler.deleteImage(BarApp.getAppContext(), photoUri);
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

    private void loadDrinks(Call<AbsolutDrinksResult> call) {

        call.enqueue(new Callback<AbsolutDrinksResult>() {
            @Override
            public void onResponse(@NonNull Call<AbsolutDrinksResult> call, @NonNull Response<AbsolutDrinksResult> response) {
                if (response.isSuccessful()) {
                    setDrinksResult(response);
                } else {
                    Toast.makeText(BarApp.getAppContext(), "server returned error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AbsolutDrinksResult> call, @NonNull Throwable t) {
                // the network call was a failure
                if (t instanceof NoConnectivityException) {
                    // No internet connection
                    Toast.makeText(BarApp.getAppContext(), "no internet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BarApp.getAppContext(), "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setDrinksResult(Response<AbsolutDrinksResult> response) {
        AbsolutDrinksResult result = response.body();
        mTotalResult = Objects.requireNonNull(result).getTotalResult();
        if (mObservableRemoteDrinks.getValue() != null && mNextLink != null
                && !mNextLink.equals(Objects.requireNonNull(result).getNext())) {
            List<DrinkEntity> currentDrinksList = mObservableRemoteDrinks.getValue();
            currentDrinksList.addAll(Objects.requireNonNull(result).getResult());
            mObservableRemoteDrinks.postValue(currentDrinksList);
        } else {
            mObservableRemoteDrinks.postValue(Objects.requireNonNull(result).getResult());
        }
        mNextLink = Objects.requireNonNull(result).getNext();
    }

    public enum QueryType {
        SEARCH_BY_NAME, SEARCH_BY_CONDITIONS, CURRENT
    }

    private static final class SingletonHolder {
        private static final BarDataRepository INSTANCE = new BarDataRepository();

        private SingletonHolder() {
        }
    }

}
