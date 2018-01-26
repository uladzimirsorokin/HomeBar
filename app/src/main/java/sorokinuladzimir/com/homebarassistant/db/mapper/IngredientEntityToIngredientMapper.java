package sorokinuladzimir.com.homebarassistant.db.mapper;

import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;

public class IngredientEntityToIngredientMapper extends Mapper<Ingredient, IngredientEntity> {

    private static IngredientEntityToIngredientMapper sInstance;

    static {
        sInstance = new IngredientEntityToIngredientMapper();
    }

    private IngredientEntityToIngredientMapper () {}

    public static IngredientEntityToIngredientMapper getInstance() {
        return sInstance;
    }

    @Override
    public IngredientEntity map(Ingredient value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ingredient reverseMap(IngredientEntity value) {
        Ingredient ingredient = new Ingredient();
        ingredient.name = value.name;
        ingredient.description = value.description;
        return ingredient;
    }
}
