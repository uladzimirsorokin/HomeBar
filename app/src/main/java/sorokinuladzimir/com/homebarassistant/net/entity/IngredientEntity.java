package sorokinuladzimir.com.homebarassistant.net.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by 1 on 10/18/2016.
 */


public class IngredientEntity implements Serializable {

    @SerializedName("textPlain")
    private String name;

    private String description;

    private String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
