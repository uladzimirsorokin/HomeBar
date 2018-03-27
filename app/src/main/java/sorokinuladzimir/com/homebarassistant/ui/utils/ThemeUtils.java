package sorokinuladzimir.com.homebarassistant.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import sorokinuladzimir.com.homebarassistant.R;

public class ThemeUtils {

    public final static int BASE_THEME = 0;
    public final static int CUSTOM_THEME = 1;
    public final static int BRIGHT_THEME = 2;

    public static void changeToTheme(Activity activity, int theme) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("currentTheme", theme);
        editor.commit();
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (activity.getPreferences(Context.MODE_PRIVATE).getInt("currentTheme", 0)) {
            default:
            case BASE_THEME:
                activity.setTheme(R.style.BaseAppTheme);
                break;
            case CUSTOM_THEME:
                activity.setTheme(R.style.CustomAppTheme);
                break;
            case BRIGHT_THEME:
                activity.setTheme(R.style.BrightAppTheme);
                break;
        }
    }


}
