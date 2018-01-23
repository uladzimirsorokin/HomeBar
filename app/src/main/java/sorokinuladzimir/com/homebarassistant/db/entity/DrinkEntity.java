package sorokinuladzimir.com.homebarassistant.db.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by 1 on 10/18/2016.
 */

public class DrinkEntity implements Serializable {

    public int drinkId;

    @SerializedName("id")
    private String imageId;
    private String name;

    @SerializedName("descriptionPlain")
    private String description;

    private String color;
    private int rating;
    private boolean isAlcoholic;
    private boolean isCarbonated;

    @SerializedName("servedIn")
    private Glass servedIn;

    private ArrayList<IngredientEntity> ingredients;
    private ArrayList<Taste> tastes;

    public String getId() {
        return imageId;
    }

    public void setId(String id) {
        this.imageId = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public int getRating() {
        return rating;
    }

    public boolean isAlcoholic() {
        return isAlcoholic;
    }

    public boolean isCarbonated() {
        return isCarbonated;
    }

    public Glass getServedIn() {
        return servedIn;
    }

    public ArrayList<IngredientEntity> getIngredients() {
        return ingredients;
    }

    public ArrayList<Taste> getTastes() {
        return tastes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setAlcoholic(boolean alcoholic) {
        isAlcoholic = alcoholic;
    }

    public void setCarbonated(boolean carbonated) {
        isCarbonated = carbonated;
    }

    public void setServedIn(Glass servedIn) {
        this.servedIn = servedIn;
    }

    public void setIngredients(ArrayList<IngredientEntity> ingredients) {
        this.ingredients = ingredients;
    }

    public void setTastes(ArrayList<Taste> tastes) {
        this.tastes = tastes;
    }
}
