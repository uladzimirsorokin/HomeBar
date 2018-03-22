package sorokinuladzimir.com.homebarassistant.repository;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

public interface Repository {
    List<Drink> getAllDrinks();
    Drink getDrink(Long drinkId);
    void deleteDrink(Long drinkId);
    void updateDrink(Drink drink);

    List<Ingredient> getAllIngredients();
    Ingredient getIngredient(Long ingredientId);
    void deleteIngredient(Long ingredientId);
    void updateIngredient(Ingredient drink);

    List<WholeCocktail> getDrinkIngredients(Long drinkId);
}
