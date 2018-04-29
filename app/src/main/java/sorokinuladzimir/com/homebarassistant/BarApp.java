package sorokinuladzimir.com.homebarassistant;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

import ru.terrakok.cicerone.Cicerone;
import ru.terrakok.cicerone.NavigatorHolder;
import ru.terrakok.cicerone.Router;
import sorokinuladzimir.com.homebarassistant.db.CocktailsDatabase;


/**
 * Created by sorok on 17.10.2017.
 */

public class BarApp extends Application {

    private static BarApp sInstance;
    private Cicerone<Router> cicerone;
    private AppExecutors mAppExecutors;

    public static String sDefSystemLanguage;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            sDefSystemLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else{
            //noinspection deprecation
            sDefSystemLanguage = getResources().getConfiguration().locale.getLanguage();
        }

        sInstance = this;
        cicerone = Cicerone.create();
        mAppExecutors = new AppExecutors();
    }

    public NavigatorHolder getNavigatorHolder() {
        return cicerone.getNavigatorHolder();
    }

    public Router getRouter() {
        return cicerone.getRouter();
    }

    public static BarApp getInstance() {
        return sInstance;
    }

    public CocktailsDatabase getDatabase() {
        return CocktailsDatabase.getInstance(this, mAppExecutors);
    }

    public BarDataRepository getBarRepository() {
        return BarDataRepository.getInstance(getDatabase(), mAppExecutors, getApplicationContext());
    }

    public AppExecutors getExecutors() {
        return mAppExecutors;
    }
}
