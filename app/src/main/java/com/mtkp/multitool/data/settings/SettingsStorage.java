package com.mtkp.multitool.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.mtkp.multitool.R;

import java.util.Locale;

/**
 * Локальное хранилище настроек.
 *
 * Здесь мы сохраняем и читаем данные через SharedPreferences.
 * Пока это простое хранилище на устройстве без базы данных и сервера.
 */
public class SettingsStorage {

    private static final String PREFS_NAME = "multitool_settings";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LANGUAGE_TAG = "language_tag";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_AVATAR_RES_ID = "avatar_res_id";
    private static final String KEY_ACCOUNT_CREATED = "account_created";

    private final SharedPreferences prefs;

    /**
     * Создаём объект хранилища и сразу получаем доступ к SharedPreferences.
     */
    public SettingsStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Получить сохранённый режим темы.
     */
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Сохранить выбранный режим темы.
     */
    public void setThemeMode(int themeMode) {
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    /**
     * Получить сохранённый язык.
     */
    public String getLanguageTag() {
        String defaultTag = Locale.getDefault().getLanguage().equals("ru") ? "ru" : "en";
        return prefs.getString(KEY_LANGUAGE_TAG, defaultTag);
    }

    /**
     * Сохранить выбранный язык.
     */
    public void setLanguageTag(String languageTag) {
        prefs.edit().putString(KEY_LANGUAGE_TAG, languageTag).apply();
    }

    /**
     * Получить сохранённое имя пользователя.
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    /**
     * Сохранить имя пользователя.
     */
    public void setUserName(String userName) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    /**
     * Получить выбранную иконку аватара.
     */
    public int getAvatarResId() {
        return prefs.getInt(KEY_AVATAR_RES_ID, R.drawable.ic_account_box);
    }

    /**
     * Сохранить выбранную иконку аватара.
     */
    public void setAvatarResId(int avatarResId) {
        prefs.edit().putInt(KEY_AVATAR_RES_ID, avatarResId).apply();
    }

    /**
     * Проверить, создавался ли локальный аккаунт.
     */
    public boolean isAccountCreated() {
        return prefs.getBoolean(KEY_ACCOUNT_CREATED, false);
    }

    /**
     * Сохранить факт создания локального аккаунта.
     */
    public void setAccountCreated(boolean accountCreated) {
        prefs.edit().putBoolean(KEY_ACCOUNT_CREATED, accountCreated).apply();
    }

    /**
     * Очистить локальные данные профиля при выходе из аккаунта.
     */
    public void clearLocalAccount() {
        prefs.edit()
                .putBoolean(KEY_ACCOUNT_CREATED, false)
                .putString(KEY_USER_NAME, "")
                .putInt(KEY_AVATAR_RES_ID, R.drawable.ic_account_box)
                .apply();
    }
}



