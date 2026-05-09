package com.mtkp.multitool;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.mtkp.multitool.data.settings.SettingsStorage;

/**
 * Класс приложения.
 *
 * Он запускается раньше, чем любая Activity, и нужен для того,
 * чтобы применить сохранённые общие настройки сразу при старте приложения:
 * тему оформления и язык интерфейса.
 */
public class MultiToolApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SettingsStorage storage = new SettingsStorage(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(storage.getThemeMode());
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(storage.getLanguageTag())
        );
    }
}


