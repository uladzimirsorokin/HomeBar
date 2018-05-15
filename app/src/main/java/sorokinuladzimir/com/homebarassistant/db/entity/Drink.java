package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;

@Entity
@TypeConverters(TasteConverter.class)
public class Drink implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;

    private String description;

    private ArrayList<Taste> tastes;

    private String image;

    @Embedded
    private Glass glass;

    private int rating;

    private boolean isAlcoholic;

    private boolean isCarbonated;

    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public ArrayList<Taste> getTastes() {
        return tastes;
    }

    public void setTastes(ArrayList<Taste> tastes) {
        this.tastes = tastes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Glass getGlass() {
        return glass;
    }

    public void setGlass(Glass glass) {
        this.glass = glass;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
