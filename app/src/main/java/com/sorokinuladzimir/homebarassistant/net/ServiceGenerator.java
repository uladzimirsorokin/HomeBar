package com.sorokinuladzimir.homebarassistant.net;

import android.support.annotation.NonNull;

import com.sorokinuladzimir.homebarassistant.BarApp;
import com.sorokinuladzimir.homebarassistant.Constants;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ServiceGenerator {

    private final OkHttpClient.Builder httpClient;
    private final AbsolutDrinksApi absolutDrinksService;

    private ServiceGenerator() {
        httpClient = getOkHttpClient();
        absolutDrinksService = createService(AbsolutDrinksApi.class, Constants.Uri.ABSOLUT_DRINKS_ROOT);
    }

    public static ServiceGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private OkHttpClient.Builder getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new ConnectivityInterceptor())
                .addInterceptor(new DrinksRequestInterceptor(getLanguage()));
    }

    private String getLanguage() {
        String lang = BarApp.getLang();
        if (lang.equals("ru")) {
            return lang;
        } else {
            return "en";
        }
    }

    public AbsolutDrinksApi getAbsolutDrinksService() {
        return absolutDrinksService;
    }


    private <S> S createService(Class<S> serviceClass, String baseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create());
        return builder.build().create(serviceClass);
    }

    private static final class SingletonHolder {
        private static final ServiceGenerator INSTANCE = new ServiceGenerator();

        private SingletonHolder() {
        }
    }

    public class DrinksRequestInterceptor implements Interceptor {

        private final String lang;

        DrinksRequestInterceptor(String lang) {
            this.lang = lang;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter(Constants.Extra.API_KEY, Constants.Keys.ABSOLUT_API_KEY)
                    .addQueryParameter(Constants.Extra.LANG, lang)
                    .build();
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);
            Request request = requestBuilder.build();

            return chain.proceed(request);
        }

    }

    public class ConnectivityInterceptor implements Interceptor {

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            if (!NetworkUtil.isOnline(BarApp.getAppContext())) {
                throw new NoConnectivityException();
            }

            Request.Builder builder = chain.request().newBuilder();
            return chain.proceed(builder.build());
        }

    }

}