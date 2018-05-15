package sorokinuladzimir.com.homebarassistant.ui.utils;

import java.util.Arrays;

import sorokinuladzimir.com.homebarassistant.db.entity.Glass;


public class SpinnerGlassMatcher {

    private SpinnerGlassMatcher() {
    }

    public static int matchGlass(String[] glasses, Glass glass) {
        return glass != null ? Arrays.asList(glasses).indexOf(glass.getGlassName()) : 0;
    }
}
