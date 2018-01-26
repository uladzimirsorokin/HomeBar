package sorokinuladzimir.com.homebarassistant.db.mapper;

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
        drink.name = value.name;
        drink.tastes = value.tastes;
        drink.description = value.description;
        drink.image = value.id;
        drink.ingredients = IngredientEntityToIngredientMapper.getInstance().reverseMap(value.ingredients);
        /* TODO
        *  подумать как состыковать стаканы и зачем мне матчить ингридиенты если они не пойдут в базу...или пойдут...))
        * */

        return drink;
    }
}
