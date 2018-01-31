package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;

@Entity
@TypeConverters(TasteConverter.class)
public class Drink implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    public String name;

    public String description;

    public ArrayList<Taste> tastes;

    public String image;

    @Ignore
    public List<Ingredient> ingredients;

    @Embedded
    public Glass glass;

}
