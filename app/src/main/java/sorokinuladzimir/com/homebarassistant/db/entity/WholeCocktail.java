package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.ColumnInfo;

public class WholeCocktail {

    public int id;

    @ColumnInfo(name="ingredient")
    public String ingredientName;

    @ColumnInfo(name="drink")
    public String drinkName;

    public int amount;

    public String unit;

}
