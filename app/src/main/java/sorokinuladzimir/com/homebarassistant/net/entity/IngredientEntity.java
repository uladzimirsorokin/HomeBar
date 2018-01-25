package sorokinuladzimir.com.homebarassistant.net.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by 1 on 10/18/2016.
 */


public class IngredientEntity implements Serializable {

    @SerializedName("textPlain")
    public String name;

    public String description;

    public String url;

}
