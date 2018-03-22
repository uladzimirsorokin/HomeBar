package sorokinuladzimir.com.homebarassistant.db.mapper;

import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.ui.utils.IngredientParcer;

public class RawIngredientToWholeCocktailMapper extends Mapper<WholeCocktail, Ingredient> {

    private static RawIngredientToWholeCocktailMapper sInstance;

    static {
        sInstance = new RawIngredientToWholeCocktailMapper();
    }

    private RawIngredientToWholeCocktailMapper() {}

    public static RawIngredientToWholeCocktailMapper getInstance() {
        return sInstance;
    }

    @Override
    public Ingredient map(WholeCocktail value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WholeCocktail reverseMap(Ingredient value) {
        WholeCocktail wholeCocktail = new WholeCocktail();
        wholeCocktail.setIngredientName(IngredientParcer.parseName(value.getName()));
        wholeCocktail.setUnit(IngredientParcer.parseUnit(value.getName()));
        wholeCocktail.setAmount(IngredientParcer.parseAmount(value.getName()));
        return wholeCocktail;
    }
}
