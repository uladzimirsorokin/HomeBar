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
    private Long id;

    private String amount;

    private String unit;

    @ColumnInfo(name="drink_id")
    private Long drinkId;

    @ColumnInfo(name="ingredient_id")
    private Long ingredientId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(Long drinkId) {
        this.drinkId = drinkId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }
}
