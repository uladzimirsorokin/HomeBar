package sorokinuladzimir.com.homebarassistant;

import android.app.Application;

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

    @Override
    public void onCreate() {
        super.onCreate();
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

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase(),mAppExecutors);
    }

}
