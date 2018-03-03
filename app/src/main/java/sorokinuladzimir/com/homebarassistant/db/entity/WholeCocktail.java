package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.ColumnInfo;

import java.util.Objects;

public class WholeCocktail {

    @ColumnInfo(name="join_id")
    public Long jointableId;

    @ColumnInfo(name="id")
    public Long ingredientId;

    @ColumnInfo(name="ingredient")
    public String ingredientName;

    public String amount;

    public String unit;

    public String image;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WholeCocktail that = (WholeCocktail) o;
        return Objects.equals(jointableId, that.jointableId) &&
                Objects.equals(ingredientId, that.ingredientId) &&
                Objects.equals(ingredientName, that.ingredientName) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jointableId, ingredientId, ingredientName, amount, unit, image);
    }
}
