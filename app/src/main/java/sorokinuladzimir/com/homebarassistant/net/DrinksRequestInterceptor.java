package sorokinuladzimir.com.homebarassistant.net;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import sorokinuladzimir.com.homebarassistant.Constants;

public class DrinksRequestInterceptor implements Interceptor {

    private String lang;

    DrinksRequestInterceptor(String lang) {
        this.lang = lang;
    }

    /*
                .addQueryParameter("start", String.valueOf(start))
                .addQueryParameter("pageSize", String.valueOf(pageSize))
    * */

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();
        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("apiKey", Constants.Keys.ABSOLUT_API_KEY)
                .addQueryParameter("lang", lang)
                .build();
        Request.Builder requestBuilder = original.newBuilder()
                .url(url);
        Request request = requestBuilder.build();

        return chain.proceed(request);
    }

}