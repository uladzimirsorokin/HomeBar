package sorokinuladzimir.com.homebarassistant.db.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Drink.class,
                parentColumns = "id",
                childColumns = "drink_id"),

        @ForeignKey(entity = Ingredient.class,
                parentColumns = "id",
                childColumns = "ingredient_id")})
public class DrinkIngredientJoin {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int amount;

    public String unit;

    @ColumnInfo(name="drink_id")
    public int drinkId;

    @ColumnInfo(name="ingredient_id")
    public int ingredientId;
}
