package sorokinuladzimir.com.homebarassistant.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Objects;

public class NetworkUtil {

    private NetworkUtil() {
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

}
