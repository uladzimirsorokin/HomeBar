package sorokinuladzimir.com.homebarassistant;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import ru.terrakok.cicerone.Cicerone;
import ru.terrakok.cicerone.NavigatorHolder;
import ru.terrakok.cicerone.Router;
import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;


public class BarApp extends Application {

    private static BarApp sInstance;
    private static Context context;
    private static String sDefSystemLanguage;
    private static AppExecutors mAppExecutors;
    private Cicerone<Router> cicerone;

    public static String getLang() {
        return sDefSystemLanguage;
    }

    public static Context getAppContext() {
        return context;
    }

    public static BarApp getInstance() {
        return sInstance;
    }

    public static CocktailsDatabase getDatabase() {
        return CocktailsDatabase.getInstance();
    }

    public static BarDataRepository getBarRepository() {
        return BarDataRepository.getInstance();
    }

    public static AppExecutors getExecutors() {
        return mAppExecutors;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDefSystemLanguage = getSystemLang();
        sInstance = this;
        context = getApplicationContext();
        cicerone = Cicerone.create();
        mAppExecutors = new AppExecutors();
    }

    private String getSystemLang() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return getResources().getConfiguration().getLocales().get(0).getLanguage();
        else {
            return getResources().getConfiguration().locale.getLanguage();
        }
    }

    public NavigatorHolder getNavigatorHolder() {
        return cicerone.getNavigatorHolder();
    }

    public Router getRouter() {
        return cicerone.getRouter();
    }
}
