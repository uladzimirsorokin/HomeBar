package sorokinuladzimir.com.homebarassistant.net.entity;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.db.entity.Taste;

/**
 * Created by 1 on 10/18/2016.
 */

public class DrinkEntity implements Serializable {


    public String id;

    public String name;

    @SerializedName("descriptionPlain")
    public String description;

    public String color;

    public int rating;

    public boolean isAlcoholic;

    public boolean isCarbonated;

    @SerializedName("servedIn")
    public Glass servedIn;

    public ArrayList<IngredientEntity> ingredients;

    public ArrayList<Taste> tastes;
}
