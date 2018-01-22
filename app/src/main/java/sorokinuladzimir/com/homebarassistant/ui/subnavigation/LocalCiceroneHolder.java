package sorokinuladzimir.com.homebarassistant.ui.subnavigation;

import java.util.HashMap;

import ru.terrakok.cicerone.Cicerone;
import ru.terrakok.cicerone.Router;

/**
 * Created by sorok on 18.10.2017.
 */

public class LocalCiceroneHolder {
    private HashMap<String, Cicerone<Router>> containers;

    public LocalCiceroneHolder() {
        containers = new HashMap<>();
    }

    public Cicerone<Router> getCicerone(String containerTag) {
        if (!containers.containsKey(containerTag)) {
            containers.put(containerTag, Cicerone.create());
        }
        return containers.get(containerTag);
    }
}
