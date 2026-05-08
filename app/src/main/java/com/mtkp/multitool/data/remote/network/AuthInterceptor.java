package com.mtkp.multitool.data.remote.network;

import androidx.annotation.NonNull;

import com.mtkp.multitool.data.settings.SettingsStorage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Подставляет Authorization заголовок с JWT токеном в каждый запрос.
 */
public class AuthInterceptor implements Interceptor {

    private final SettingsStorage settingsStorage;

    public AuthInterceptor(SettingsStorage settingsStorage) {
        this.settingsStorage = settingsStorage;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = settingsStorage.getAuthToken();

        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request withAuth = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(withAuth);
    }
}

