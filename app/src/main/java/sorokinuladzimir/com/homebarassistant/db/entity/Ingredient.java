package sorokinuladzimir.com.homebarassistant.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Ingredient {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public String description;

    public String image;

}
