package com.mtkp.multitool.data.remote.network;

import android.content.Context;

import com.mtkp.multitool.data.settings.SettingsStorage;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Фабрика Retrofit клиента.
 */
public class ApiClient {

    // Для эмулятора Android Studio (если backend запущен локально на ПК):
    // private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";
    private static final String BASE_URL = "http://bluetiful-cymophane293313.vm-host.com:8090/api/v1/";

    private final BackendApiService service;

    public ApiClient(Context context) {
        SettingsStorage storage = new SettingsStorage(context.getApplicationContext());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(storage))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.service = retrofit.create(BackendApiService.class);
    }

    public BackendApiService service() {
        return service;
    }
}

