package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.ColumnInfo;

import java.util.Objects;

public class WholeCocktail {

    @ColumnInfo(name = "join_id")
    private Long jointableId;

    @ColumnInfo(name = "id")
    private Long ingredientId;

    @ColumnInfo(name = "ingredient")
    private String ingredientName;

    private String amount;

    private String unit;

    private String image;

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

    public Long getJointableId() {
        return jointableId;
    }

    public void setJointableId(Long jointableId) {
        this.jointableId = jointableId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
