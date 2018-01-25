package sorokinuladzimir.com.homebarassistant.db.converter;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.db.entity.Taste;

public class TasteConverter {

    @TypeConverter
    public static ArrayList<Taste> toList(String value) {
        Type listType = new TypeToken<ArrayList<Taste>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String toString(ArrayList<Taste> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
