package com.mtkp.multitool.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.SettingsDao;
import com.mtkp.multitool.data.local.SettingsEntity;
import com.mtkp.multitool.data.remote.ApiRequestException;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.AuthDto;
import com.mtkp.multitool.data.settings.SettingsStorage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SettingsRepository — каталог доступа к настройкам приложения.
 *
 * Этот класс объединяет два источника данных:
 * - SettingsStorage (SharedPreferences) — для быстрого доступа к оперативным данным (тема, язык)
 * - SettingsDao (Room) — для долгосрочного локального хранилища
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

    private static final String TAG = "SettingsRepository";

    private final Context appContext;
    private final SettingsStorage settingsStorage;
    private final SettingsDao settingsDao;
    private final RemoteDataSource remoteDataSource;
    // Один поток гарантирует последовательную запись настроек в БД без гонок.
    private final ExecutorService dbExecutor;
    // Отдельный исполнитель для сетевых операций.
    private final ExecutorService networkExecutor;
    // Callback'и для UI должны выполняться на главном потоке.
    private final Handler mainHandler;

    /**
     * Коллбек для результатов серверной авторизации.
     */
    public interface AuthCallback {
        void onSuccess(AuthDto authDto);

        void onError(String message);
    }

    /**
     * Создать репозиторий с доступом к обоим хранилищам.
     *
     * @param context контекст приложения для создания SharedPreferences и БД
     */
    public SettingsRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.settingsStorage = new SettingsStorage(appContext);
        this.settingsDao = AppDatabase.getInstance(appContext).settingsDao();
        this.remoteDataSource = new RemoteDataSource(appContext);
        this.dbExecutor = Executors.newSingleThreadExecutor();
        this.networkExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ========== СЕТЕВАЯ АУТЕНТИФИКАЦИЯ (PostgreSQL) ==========

    /**
     * Зарегистрировать пользователя на сервере через REST API.
     *
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @param callback обработчик результата
     */
    public void registerOnServer(String username, String email, String password, AuthCallback callback) {
        Log.d(TAG, "registerOnServer start: username=" + username + ", email=" + email);
        networkExecutor.execute(() -> {
            try {
                AuthDto authDto = remoteDataSource.register(username, email, password);
                persistAuthState(authDto, username, email);
                postAuthSuccess(callback, authDto);
            } catch (Exception e) {
                Log.e(TAG, "registerOnServer failed", e);
                postAuthError(callback, e, false);
            }
        });
    }

    /**
     * Войти в аккаунт на сервере через REST API.
     *
     * @param email электронная почта
     * @param password пароль
     * @param callback обработчик результата
     */
    public void loginOnServer(String email, String password, AuthCallback callback) {
        Log.d(TAG, "loginOnServer start: email=" + email);
        networkExecutor.execute(() -> {
            try {
                AuthDto authDto = remoteDataSource.login(email, password);
                persistAuthState(authDto, authDto.username, email);
                postAuthSuccess(callback, authDto);
            } catch (Exception e) {
                Log.e(TAG, "loginOnServer failed", e);
                postAuthError(callback, e, true);
            }
        });
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
     * Получить email текущего пользователя.
     */
    public String getEmail() {
        return settingsStorage.getEmail();
    }

    /**
     * Получить id текущего пользователя.
     */
    public long getUserId() {
        return settingsStorage.getUserId();
    }

    /**
     * Установить имя пользователя.
     * Если пользователь авторизован (currentUserId != -1), также сохраняется в БД.
     */
    public void setUserName(String userName) {
        settingsStorage.setUserName(userName);
        saveSettingToDb("username", userName);
    }

    /**
     * Сохранить email текущего пользователя.
     */
    public void setEmail(String email) {
        settingsStorage.setEmail(email);
        saveSettingToDb("email", email);
    }

    /**
     * Сохранить id текущего пользователя.
     */
    public void setUserId(long userId) {
        settingsStorage.setUserId(userId);
        saveSettingToDb("user_id", String.valueOf(userId));
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
     * Проверить, есть ли у пользователя хотя бы одна сохранённая auth-сессия.
     */
    public boolean isAuthenticated() {
        String token = settingsStorage.getAuthToken();
        return token != null && !token.trim().isEmpty();
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
        saveSettingToDb("email", "");
        saveSettingToDb("user_id", "-1");
        saveSettingToDb("auth_token", "");
        saveSettingToDb("token_expires_at", "-1");
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
            SettingsEntity setting = new SettingsEntity();
            setting.key = key;
            setting.value = value;
            setting.lastUpdated = System.currentTimeMillis();

            settingsDao.insertOrReplace(setting);
        });
    }

    /**
     * Обновить auth-состояние после успешной регистрации/логина.
     */
    private void persistAuthState(AuthDto authDto, String fallbackUsername, String fallbackEmail) {
        String username = authDto.username;
        if (username == null || username.trim().isEmpty()) {
            username = fallbackUsername == null ? "" : fallbackUsername.trim();
        }

        String email = authDto.email;
        if (email == null || email.trim().isEmpty()) {
            email = fallbackEmail == null ? "" : fallbackEmail.trim();
        }

        String token = authDto.token == null ? "" : authDto.token;
        Long tokenExpiresAt = authDto.tokenExpiresAt;

        settingsStorage.setUserId(authDto.id);
        settingsStorage.setUserName(username);
        settingsStorage.setEmail(email);
        settingsStorage.setAuthToken(token);
        settingsStorage.setTokenExpiresAt(tokenExpiresAt);
        settingsStorage.setAccountCreated(true);

        saveSettingToDb("user_id", String.valueOf(authDto.id));
        saveSettingToDb("username", username);
        saveSettingToDb("email", email);
        saveSettingToDb("auth_token", token);
        saveSettingToDb("token_expires_at", String.valueOf(tokenExpiresAt == null ? -1L : tokenExpiresAt));
        saveSettingToDb("account_created", "true");
    }

    /**
     * Вернуть результат авторизации на главный поток.
     */
    private void postAuthSuccess(AuthCallback callback, AuthDto authDto) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onSuccess(authDto));
    }

    /**
     * Вернуть ошибку авторизации на главный поток.
     */
    private void postAuthError(AuthCallback callback, Exception e, boolean loginFlow) {
        if (callback == null) {
            return;
        }
        String message = resolveAuthErrorMessage(e, loginFlow);
        mainHandler.post(() -> callback.onError(message));
    }

    /**
     * Преобразовать сетевую ошибку в понятный текст для пользователя.
     */
    private String resolveAuthErrorMessage(Exception e, boolean loginFlow) {
        if (e instanceof ApiRequestException) {
            int code = ((ApiRequestException) e).getCode();
            if (code == 401) {
                return loginFlow
                        ? appContext.getString(com.mtkp.multitool.R.string.auth_invalid_credentials)
                        : appContext.getString(com.mtkp.multitool.R.string.auth_registration_failed);
            }
            if (code == 409) {
                return loginFlow
                        ? appContext.getString(com.mtkp.multitool.R.string.auth_account_unavailable)
                        : appContext.getString(com.mtkp.multitool.R.string.auth_account_exists);
            }
            if (code >= 500) {
                return appContext.getString(com.mtkp.multitool.R.string.auth_server_unavailable);
            }
            return appContext.getString(com.mtkp.multitool.R.string.auth_http_error, code);
        }

        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "Auth error";
        }
        return message;
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

