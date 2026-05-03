package com.mtkp.multitool.data.repository;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.SettingsDao;
import com.mtkp.multitool.data.local.SettingsEntity;
import com.mtkp.multitool.data.settings.SettingsStorage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SettingsRepository — каталог доступа к настройкам приложения.
 *
 * Этот класс объединяет два источника данных:
 * - SettingsStorage (SharedPreferences) — для быстрого доступа к оперативным данным (тема, язык)
 * - SettingsDao (Room) — для долгосрочного хранилища, истории, связи с пользователем
 *
 * Архитектура:
 * - UI (Presenter) → SettingsRepository → {SettingsStorage + SettingsDao}
 *
 * Паттерн:
 * 1. Когда пользователь меняет настройку → сохраняется в оба хранилища одновременно
 * 2. Когда читается настройка → читается из SharedPreferences (быстро), если нужна история — из Room
 * 3. При первом запуске приложения можно синхронизировать данные из Room в SharedPreferences
 */
public class SettingsRepository {

    private final SettingsStorage settingsStorage;
    private final SettingsDao settingsDao;
    // Один поток гарантирует последовательную запись настроек в БД без гонок.
    private final ExecutorService dbExecutor;

    /**
     * Создать репозиторий с доступом к обоим хранилищам.
     *
     * @param context контекст приложения для создания SharedPreferences и БД
     */
    public SettingsRepository(Context context) {
        this.settingsStorage = new SettingsStorage(context);
        this.settingsDao = AppDatabase.getInstance(context).settingsDao();
        this.dbExecutor = Executors.newSingleThreadExecutor();
    }

    // ========== ТЕМА ==========

    /**
     * Получить текущий режим темы.
     * Читается из SharedPreferences (быстро).
     */
    public int getThemeMode() {
        return settingsStorage.getThemeMode();
    }

    /**
     * Установить новый режим темы.
     * Сохраняется в оба хранилища: SharedPreferences (мгновенное применение) и Room (история).
     *
     * @param themeMode одна из констант AppCompatDelegate.MODE_NIGHT_*
     */
    public void setThemeMode(int themeMode) {
        // 1. Сохранить в SharedPreferences для мгновенного применения
        settingsStorage.setThemeMode(themeMode);

        // 2. Сохранить в Room для долгосрочного хранения и истории
        String themeName = themeToString(themeMode);
        saveSettingToDb("theme", themeName);
    }

    // ========== ЯЗЫК ==========

    /**
     * Получить текущий язык приложения (код: "ru", "en").
     * Читается из SharedPreferences.
     */
    public String getLanguageTag() {
        return settingsStorage.getLanguageTag();
    }

    /**
     * Установить новый язык приложения.
     * Сохраняется в оба хранилища.
     *
     * @param languageTag код языка ("ru", "en" и т.д.)
     */
    public void setLanguageTag(String languageTag) {
        // 1. Сохранить в SharedPreferences
        settingsStorage.setLanguageTag(languageTag);

        // 2. Сохранить в Room
        saveSettingToDb("language", languageTag);
    }

    // ========== ИМЯ ПОЛЬЗОВАТЕЛЯ ==========

    /**
     * Получить имя пользователя из локального профиля.
     */
    public String getUserName() {
        return settingsStorage.getUserName();
    }

    /**
     * Установить имя пользователя.
     * Если пользователь авторизован (currentUserId != -1), также сохраняется в БД.
     */
    public void setUserName(String userName) {
        settingsStorage.setUserName(userName);
        saveSettingToDb("username", userName);
    }

    // ========== АВАТАР ==========

    /**
     * Получить ID ресурса текущей аватарки.
     */
    public int getAvatarResId() {
        return settingsStorage.getAvatarResId();
    }

    /**
     * Установить аватарку по ID ресурса.
     */
    public void setAvatarResId(int avatarResId) {
        settingsStorage.setAvatarResId(avatarResId);
        // Для аватара можно не сохранять в БД, если это локальная опция
        // Но если нужна история — раскомментируй:
        // saveSettingToDb("avatar_res_id", String.valueOf(avatarResId));
    }

    // ========== СОСТОЯНИЕ АККАУНТА ==========

    /**
     * Проверить, был ли создан локальный аккаунт.
     */
    public boolean isAccountCreated() {
        return settingsStorage.isAccountCreated();
    }

    /**
     * Пометить, что локальный аккаунт создан.
     */
    public void setAccountCreated(boolean created) {
        settingsStorage.setAccountCreated(created);
        saveSettingToDb("account_created", String.valueOf(created));
    }

    /**
     * Очистить весь локальный профиль (при выходе из аккаунта).
     */
    public void clearLocalAccount() {
        settingsStorage.clearLocalAccount();
        saveSettingToDb("username", "");
        saveSettingToDb("account_created", "false");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Внутренний метод для сохранения настройки в Room.
     * Выполняется в фоновом потоке (асинхронно).
     *
     * @param key ключ настройки
     * @param value значение настройки
     */
    private void saveSettingToDb(String key, String value) {
        // Пишем в одном потоке: так не появятся дубликаты из параллельных вызовов.
        dbExecutor.execute(() -> {
            // Пока авторизация не подключена: храним только глобальные настройки.
            settingsDao.deleteByKey(key);

            SettingsEntity setting = new SettingsEntity();
            setting.key = key;
            setting.value = value;
            setting.userId = null;
            setting.lastUpdated = System.currentTimeMillis();

            settingsDao.insertOrReplace(setting);
        });
    }

    /**
     * Преобразовать код темы (AppCompatDelegate.MODE_NIGHT_*) в строку.
     */
    private String themeToString(int themeMode) {
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return "light";
            case AppCompatDelegate.MODE_NIGHT_YES:
                return "dark";
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                return "system";
        }
    }

    /**
     * Преобразовать строку темы обратно в код AppCompatDelegate.
     */
    public static int stringToTheme(String themeName) {
        switch (themeName) {
            case "light":
                return AppCompatDelegate.MODE_NIGHT_NO;
            case "dark":
                return AppCompatDelegate.MODE_NIGHT_YES;
            case "system":
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}

