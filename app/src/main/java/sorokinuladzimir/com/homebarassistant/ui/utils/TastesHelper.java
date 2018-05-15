package sorokinuladzimir.com.homebarassistant.ui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.db.entity.Taste;


public class TastesHelper {

    private TastesHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static String tastesToString(List<Taste> tastes) {
        if (tastes != null && !tastes.isEmpty()) {
            StringBuilder tastesStr = new StringBuilder(tastes.get(0).getText());
            for (int i = 1; i < tastes.size(); i++) {
                tastesStr.append(", ").append(tastes.get(i).getText());
            }
            return tastesStr.toString();
        }

        return "";
    }

    public static List<Taste> toTastesList(List<Integer> selectedIngredients, String[] tastesArray) {
        ArrayList<Taste> tastes = new ArrayList<>();
        for (int i : selectedIngredients) {
            tastes.add(new Taste(tastesArray[i]));
        }
        return tastes;
    }

    public static List<Integer> getAsIntegerList(List<Taste> tastes, String[] tastesArray) {
        ArrayList<Integer> tastesList = new ArrayList<>();
        if (tastes != null) {
            for (Taste taste : tastes) {
                int pos = Arrays.asList(tastesArray).indexOf(taste.getText());
                if (pos >= 0) tastesList.add(pos);
            }
        }
        return tastesList;
    }
}
