package sorokinuladzimir.com.homebarassistant;

import android.arch.lifecycle.LiveData;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;

public interface BarData {

    LiveData<List<Drink>> getLocalDrinks();

    void getRemoteDrinks(String requestConditions, BarDataRepository.QueryType queryType, boolean clearList);

    LiveData<Drink> getDrink(final Long drinkId);

    LiveData<List<Drink>> getDrinksByName(String searchQuery);

    LiveData<List<Drink>> getDrinksByIngredient(Long ingredientId);

    void saveRemoteDrink(DrinkEntity drink, Bitmap bitmap);

    void saveDrink(Drink drink, List<DrinkIngredientJoin> drinkIngredients);

    void deleteDrink(Long drinkId);

    LiveData<List<Ingredient>> getIngredients();

    LiveData<Ingredient> getIngredient(final Long ingredientId);

    LiveData<List<Ingredient>> getIngredientsByName(String searchQuery);

    int deleteIngredient(Long ingredientId);

    LiveData<List<WholeCocktail>> getDrinkIngredients(final Long drinkId);

    void getRemoteDrinkIngredients(String drinkId);

    void addIngredient(final Ingredient ingredient);

    LiveData<List<WholeCocktail>> getListIngredientsNames(final List<Long> ingredientIds);





    void saveImageToAlbum(Uri imageUri, int sizeForScale, boolean deleteSource, boolean ingredientImage);

    Uri createImageFile();

    void deleteImage(String imagePath);

    void resetIngredientImagePath();

    void resetDrinkImagePath();

    LiveData<String> getObservableDrinkImagePath();

    LiveData<String> getObservableIngredientImagePath();

}
