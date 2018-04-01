package sorokinuladzimir.com.homebarassistant.net;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sorokinuladzimir.com.homebarassistant.Constants;

public class RetrofitInstance {

    private static final String ROOT = Constants.Uri.ABSOLUT_DRINKS_ROOT;

    private static Retrofit sInstance;

    public static Retrofit getRetrofitInstance(Context context, String lang) {
        if (sInstance == null) {
            sInstance = new Retrofit.Builder()
                    .baseUrl(ROOT)
                    .client(getOkHttpClient(context, lang))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sInstance;
    }

    private static OkHttpClient getOkHttpClient(Context context, String lang) {
        return new OkHttpClient.Builder()
                .addInterceptor(new ConnectivityInterceptor(context))
                .addInterceptor(new DrinksRequestInterceptor(lang))
                .build();
    }
}
