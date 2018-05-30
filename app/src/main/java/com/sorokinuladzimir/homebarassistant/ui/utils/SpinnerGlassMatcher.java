package com.sorokinuladzimir.homebarassistant.ui.utils;

import com.sorokinuladzimir.homebarassistant.db.entity.Glass;

import java.util.Arrays;


public class SpinnerGlassMatcher {

    private SpinnerGlassMatcher() {
    }

    public static int matchGlass(String[] glasses, Glass glass) {
        return glass != null ? Arrays.asList(glasses).indexOf(glass.getGlassName()) : 0;
    }
}
