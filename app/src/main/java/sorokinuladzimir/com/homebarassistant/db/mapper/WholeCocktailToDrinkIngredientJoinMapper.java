package sorokinuladzimir.com.homebarassistant.db.mapper;

import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;

public class WholeCocktailToDrinkIngredientJoinMapper extends Mapper<DrinkIngredientJoin, WholeCocktail> {

    private static WholeCocktailToDrinkIngredientJoinMapper sInstance;

    static {
        sInstance = new WholeCocktailToDrinkIngredientJoinMapper();
    }

    private WholeCocktailToDrinkIngredientJoinMapper() {}

    public static WholeCocktailToDrinkIngredientJoinMapper getInstance() {
        return sInstance;
    }

    @Override
    public WholeCocktail map(DrinkIngredientJoin value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DrinkIngredientJoin reverseMap(WholeCocktail value) {
        DrinkIngredientJoin drinkIngredient = new DrinkIngredientJoin();
        drinkIngredient.id = value.jointableId;
        drinkIngredient.ingredientId = value.ingredientId;
        drinkIngredient.amount = value.amount;
        drinkIngredient.unit = value.unit;
        return drinkIngredient;
    }


}
