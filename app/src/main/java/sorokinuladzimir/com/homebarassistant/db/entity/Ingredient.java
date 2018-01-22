package sorokinuladzimir.com.homebarassistant.db.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by 1 on 10/18/2016.
 */

public class Ingredient implements Serializable {

    @SerializedName("textPlain")
    private String name;
    private String description;
    private String notes;
    private String url;

    private int amount;
    private String unit;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /*public String getTextPlain() {
        return textPlain;
    }

    public void setTextPlain(String textPlain) {
        this.textPlain = textPlain;
    }*/
}
