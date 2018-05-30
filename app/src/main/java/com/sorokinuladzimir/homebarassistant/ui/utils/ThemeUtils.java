package com.sorokinuladzimir.homebarassistant.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;

public class ThemeUtils {

    private static final int INDIGO = 0;
    private static final int PINK = 1;
    private static final int BLACK = 2;

    private ThemeUtils() {
    }

    public static void changeToTheme(Activity activity, int theme) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constants.Extra.APP_THEME, theme);
        editor.apply();
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (activity.getPreferences(Context.MODE_PRIVATE).getInt(Constants.Extra.APP_THEME, 0)) {
            default:
            case INDIGO:
                activity.setTheme(R.style.AppTheme_Indigo);
                break;
            case PINK:
                activity.setTheme(R.style.AppTheme_Purple);
                break;
            case BLACK:
                activity.setTheme(R.style.AppTheme_Black);
                break;
        }
    }


}
