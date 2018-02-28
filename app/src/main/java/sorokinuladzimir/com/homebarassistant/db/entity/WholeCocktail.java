package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.ColumnInfo;

public class WholeCocktail {

    public Long id;

    @ColumnInfo(name="ingredient")
    public String ingredientName;

    @ColumnInfo(name="drink")
    public String drinkName;

    public String amount;

    public String unit;

}
