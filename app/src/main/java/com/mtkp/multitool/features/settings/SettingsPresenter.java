package com.mtkp.multitool.features.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;

import com.mtkp.multitool.core.BasePresenter;
import com.mtkp.multitool.data.remote.dto.AuthDto;
import com.mtkp.multitool.data.repository.SettingsRepository;

/**
 * Презентер экрана настроек.
 * <p>
 * Он принимает действия пользователя, проверяет введённые данные,
 * сохраняет их через SettingsRepository в локальное хранилище (SharedPreferences)
 * и долгосрочное хранилище (Room).
 * Затем говорит View, что нужно обновиться.
 */
public class SettingsPresenter extends BasePresenter<SettingsContract.View>
        implements SettingsContract.Presenter {

    private static final String TAG = "SettingsPresenter";

    private static final int USERNAME_MIN_LEN = 3;
    private static final int USERNAME_MAX_LEN = 20;

    private final SettingsRepository repository;

    /**
     * Презентеру передаём контекст приложения.
     * Через repository презентер работает и с локальными настройками, и с REST API авторизации.
     */
    public SettingsPresenter(Context context) {
        this.repository = new SettingsRepository(context);
    }

    /**
     * Загружаем все сохранённые значения и передаём их на экран.
     */
    @Override
    public void loadSettings() {
        if (!isViewAttached()) {
            return;
        }
        view.showCurrentSettings(
                repository.getThemeMode(),
                repository.getLanguageTag(),
                repository.getUserName(),
                repository.getAvatarResId(),
                repository.isAuthenticated()
        );
    }

    /**
     * Сохраняем и применяем тему, которую выбрал пользователь.
     * Репозиторий автоматически сохранит в SharedPreferences и Room.
     */
    @Override
    public void onThemeSelected(int themeMode) {
        if (repository.getThemeMode() == themeMode) {
            return;
        }
        repository.setThemeMode(themeMode);
        if (isViewAttached()) {
            view.applyTheme(themeMode);
        }
    }

    /**
     * Сохраняем язык и просим экран обновиться.
     * Репозиторий сохранит в оба хранилища.
     */
    @Override
    public void onLanguageSelected(String languageTag) {
        if (languageTag == null || languageTag.equals(repository.getLanguageTag())) {
            return;
        }
        repository.setLanguageTag(languageTag);
        if (isViewAttached()) {
            view.applyLanguage(languageTag);
        }
    }

    /**
     * Проверяем имя пользователя и сохраняем только корректный вариант.
     */
    @Override
    public void onUserNameChanged(String userName) {
        String normalized = userName == null ? "" : userName.trim();
        if (normalized.isEmpty()) {
            repository.setUserName("");
            if (isViewAttached()) {
                view.clearUserNameError();
            }
            return;
        }

        if (normalized.length() < USERNAME_MIN_LEN || normalized.length() > USERNAME_MAX_LEN) {
            if (isViewAttached()) {
                view.showUserNameError();
            }
            return;
        }

        repository.setUserName(normalized);
        if (isViewAttached()) {
            view.clearUserNameError();
        }
    }

    /**
     * Сохраняем выбранный аватар и обновляем картинку на экране.
     */
    @Override
    public void onAvatarSelected(int avatarResId) {
        repository.setAvatarResId(avatarResId);
        if (isViewAttached()) {
            view.updateAvatarPreview(avatarResId);
        }
    }

    /**
     * Проверяем данные формы регистрации и отправляем их на сервер.
     */
    @Override
    public void onCreateAccountClicked(String userName, String email, String password, String confirmPassword) {
        String normalizedUserName = userName == null ? "" : userName.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        Log.d(TAG, "onCreateAccountClicked: usernameLen=" + normalizedUserName.length()
                + ", email=" + normalizedEmail);

        if (normalizedUserName.length() < USERNAME_MIN_LEN || normalizedUserName.length() > USERNAME_MAX_LEN) {
            if (!normalizedUserName.isEmpty()) {
                if (isViewAttached()) {
                    view.showUserNameError();
                }
                return;
            }
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            if (isViewAttached()) {
                view.showInvalidEmailError();
            }
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (isViewAttached()) {
                view.showWeakPasswordError();
            }
            return;
        }

        if (!TextUtils.equals(password, confirmPassword)) {
            if (isViewAttached()) {
                view.showPasswordMismatchError();
            }
            return;
        }

        String finalUserName = normalizedUserName;
        if (finalUserName.isEmpty()) {
            int atIndex = normalizedEmail.indexOf('@');
            finalUserName = atIndex > 0 ? normalizedEmail.substring(0, atIndex) : normalizedEmail;
            Log.d(TAG, "onCreateAccountClicked: username fallback used -> " + finalUserName);
        }

        repository.registerOnServer(finalUserName, normalizedEmail, password, new SettingsRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthDto authDto) {
                if (!isViewAttached()) {
                    return;
                }
                view.showCurrentSettings(
                        repository.getThemeMode(),
                        repository.getLanguageTag(),
                        repository.getUserName(),
                        repository.getAvatarResId(),
                        repository.isAuthenticated()
                );
                view.showAccountCreatedMessage();
            }

            @Override
            public void onError(String message) {
                if (!isViewAttached()) {
                    return;
                }
                view.showError(message);
            }
        });
    }

    /**
     * Проверяем данные формы входа и отправляем их на сервер.
     */
    @Override
    public void onLoginClicked(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            if (isViewAttached()) {
                view.showInvalidEmailError();
            }
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (isViewAttached()) {
                view.showWeakPasswordError();
            }
            return;
        }

        repository.loginOnServer(normalizedEmail, password, new SettingsRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthDto authDto) {
                if (!isViewAttached()) {
                    return;
                }
                view.showCurrentSettings(
                        repository.getThemeMode(),
                        repository.getLanguageTag(),
                        repository.getUserName(),
                        repository.getAvatarResId(),
                        repository.isAuthenticated()
                );
                view.showLoggedInMessage();
            }

            @Override
            public void onError(String message) {
                if (!isViewAttached()) {
                    return;
                }
                view.showError(message);
            }
        });
    }

    /**
     * Сбрасываем локальный профиль и возвращаем экран к форме создания аккаунта.
     */
    @Override
    public void onLogoutClicked() {
        repository.clearLocalAccount(); // Очищаем данные из model (метод из SettingsRepository)
        if (isViewAttached()) {
            view.showCurrentSettings(
                    repository.getThemeMode(),
                    repository.getLanguageTag(),
                    repository.getUserName(),
                    repository.getAvatarResId(),
                    repository.isAuthenticated()
            );
            view.showLoggedOutMessage();
        }
    }
}




