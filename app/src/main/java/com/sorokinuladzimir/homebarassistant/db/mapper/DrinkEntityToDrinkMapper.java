package com.sorokinuladzimir.homebarassistant.db.mapper;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.db.entity.Drink;
import com.sorokinuladzimir.homebarassistant.db.entity.Glass;
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity;

import java.util.ArrayList;

public class DrinkEntityToDrinkMapper extends sorokinuladzimir.com.homebarassistant.db.mapper.Mapper<Drink, DrinkEntity> {

    private static final DrinkEntityToDrinkMapper sInstance;

    static {
        sInstance = new DrinkEntityToDrinkMapper();
    }

    private DrinkEntityToDrinkMapper() {
    }

    public static DrinkEntityToDrinkMapper getInstance() {
        return sInstance;
    }

    @Override
    public DrinkEntity map(Drink value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drink reverseMap(DrinkEntity value) {
        Drink drink = new Drink();
        drink.setName(value.getName());
        drink.setTastes(new ArrayList<>(value.getTastes()));
        drink.setDescription(value.getDescription());
        drink.setImage(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + value.getId() + ".png");
        drink.setRating(value.getRating());
        drink.setAlcoholic(value.isAlcoholic());
        drink.setCarbonated(value.isCarbonated());
        Glass glass = new Glass();
        glass.setGlassName(value.getServedIn().getText());
        drink.setGlass(glass);
        return drink;
    }
}
