package com.sorokinuladzimir.homebarassistant.net.entity;


import com.google.gson.annotations.SerializedName;
import com.sorokinuladzimir.homebarassistant.db.entity.Taste;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DrinkEntity implements Serializable {


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

    private ArrayList<Taste> tastes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isAlcoholic() {
        return isAlcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        isAlcoholic = alcoholic;
    }

    public boolean isCarbonated() {
        return isCarbonated;
    }

    public void setCarbonated(boolean carbonated) {
        isCarbonated = carbonated;
    }

    public Glass getServedIn() {
        return servedIn;
    }

    public void setServedIn(Glass servedIn) {
        this.servedIn = servedIn;
    }

    public List<Taste> getTastes() {
        return tastes;
    }

    public void setTastes(List<Taste> tastes) {
        this.tastes = new ArrayList<>(tastes);
    }
}
