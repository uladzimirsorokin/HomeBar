package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.db.converter.TasteConverter;

@Entity
@TypeConverters(TasteConverter.class)
public class Drink {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String description;

    public ArrayList<Taste> tastes;

    public String image;

    @Embedded
    public Glass glass;


}
