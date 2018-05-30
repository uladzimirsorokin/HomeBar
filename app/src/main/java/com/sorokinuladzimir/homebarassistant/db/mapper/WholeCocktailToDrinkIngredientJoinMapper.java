package com.sorokinuladzimir.homebarassistant.db.mapper;

import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin;
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail;


public class WholeCocktailToDrinkIngredientJoinMapper extends sorokinuladzimir.com.homebarassistant.db.mapper.Mapper<DrinkIngredientJoin, WholeCocktail> {

    private static final WholeCocktailToDrinkIngredientJoinMapper sInstance;

    static {
        sInstance = new WholeCocktailToDrinkIngredientJoinMapper();
    }

    private WholeCocktailToDrinkIngredientJoinMapper() {
    }

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
        drinkIngredient.setId(value.getJointableId());
        drinkIngredient.setIngredientId(value.getIngredientId());
        drinkIngredient.setAmount(value.getAmount());
        drinkIngredient.setUnit(value.getUnit());
        return drinkIngredient;
    }


}
