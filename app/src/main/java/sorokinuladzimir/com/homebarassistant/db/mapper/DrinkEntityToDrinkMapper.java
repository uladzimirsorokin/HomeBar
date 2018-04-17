package sorokinuladzimir.com.homebarassistant.db.mapper;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;

public class DrinkEntityToDrinkMapper extends Mapper<Drink,DrinkEntity> {

    private static DrinkEntityToDrinkMapper sInstance;

    static {
        sInstance = new DrinkEntityToDrinkMapper();
    }

    private DrinkEntityToDrinkMapper () {}

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
        drink.setTastes(value.getTastes());
        drink.setDescription(value.getDescription());
        drink.setImage(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + value.getId() + ".png");
        drink.setRating(value.getRating());
        drink.setAlcoholic(value.isAlcoholic());
        drink.setCarbonated(value.isCarbonated());

        return drink;
    }
}
