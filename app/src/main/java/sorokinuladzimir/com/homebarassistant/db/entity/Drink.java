package sorokinuladzimir.com.homebarassistant.db.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by 1 on 10/18/2016.
 */

public class Drink implements Serializable {

    private String id;
    private String name;

    @SerializedName("descriptionPlain")
    private String description;

    private String color;
    private int rating;
    private boolean isAlcoholic;
    private boolean isCarbonated;

    @SerializedName("servedIn")
    private Glass servedIn;

    private ArrayList<Ingredient> ingredients;
    private ArrayList<Taste> tastes;

    private String notes;
    private ArrayList<Integer> linkedDrinks;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ArrayList<Integer> getLinkedDrinks() {
        return linkedDrinks;
    }

    public void setLinkedDrinks(ArrayList<Integer> linkedDrinks) {
        this.linkedDrinks = linkedDrinks;
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

    public ArrayList<Ingredient> getIngredients() {
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

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setTastes(ArrayList<Taste> tastes) {
        this.tastes = tastes;
    }
}
