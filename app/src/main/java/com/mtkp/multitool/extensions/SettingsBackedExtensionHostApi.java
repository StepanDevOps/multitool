package com.mtkp.multitool.extensions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Простая реализация host API для расширений через отдельный SharedPreferences namespace.
 */
public class SettingsBackedExtensionHostApi implements ExtensionHostApi {

    private static final String PREFS_NAME = "extensions_runtime";

    private final SharedPreferences prefs;

    public SettingsBackedExtensionHostApi(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void log(String tag, String message) {
        Log.d(tag == null ? "Extension" : tag, message == null ? "" : message);
    }

    @Override
    public String getSetting(String key, String defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        return prefs.getString(key, defaultValue);
    }

    @Override
    public void putSetting(String key, String value) {
        if (key == null) {
            return;
        }
        prefs.edit().putString(key, value).apply();
    }

    @Override
    public void removeSetting(String key) {
        if (key == null) {
            return;
        }
        prefs.edit().remove(key).apply();
    }
}

