package sorokinuladzimir.com.homebarassistant.db.mapper;

import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;

public class IngredientToWholeCocktailMapper extends Mapper<WholeCocktail, Ingredient> {

    private static IngredientToWholeCocktailMapper sInstance;

    static {
        sInstance = new IngredientToWholeCocktailMapper();
    }

    private IngredientToWholeCocktailMapper() {}

    public static IngredientToWholeCocktailMapper getInstance() {
        return sInstance;
    }

    @Override
    public Ingredient map(WholeCocktail value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WholeCocktail reverseMap(Ingredient value) {
        WholeCocktail wholeCocktail = new WholeCocktail();
        wholeCocktail.setIngredientName(value.getName());
        wholeCocktail.setIngredientId(value.getId());
        return wholeCocktail;
    }
}
