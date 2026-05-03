package com.mtkp.multitool.features.settings;

import android.text.TextUtils;
import android.util.Patterns;

import com.mtkp.multitool.core.BasePresenter;
import com.mtkp.multitool.data.settings.SettingsStorage;

/**
 * Презентер экрана настроек.
 * <p>
 * Он принимает действия пользователя, проверяет введённые данные,
 * сохраняет их в локальное хранилище и говорит View, что нужно показать.
 */
public class SettingsPresenter extends BasePresenter<SettingsContract.View>
        implements SettingsContract.Presenter {

    private static final int USERNAME_MIN_LEN = 3;
    private static final int USERNAME_MAX_LEN = 20;

    private final SettingsStorage storage;

    /**
     * Презентеру передаём хранилище, чтобы он мог читать и сохранять настройки.
     */
    public SettingsPresenter(SettingsStorage storage) {
        this.storage = storage;
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
                storage.getThemeMode(),
                storage.getLanguageTag(),
                storage.getUserName(),
                storage.getAvatarResId(),
                storage.isAccountCreated()
        );
    }

    /**
     * Сохраняем и применяем тему, которую выбрал пользователь.
     */
    @Override
    public void onThemeSelected(int themeMode) {
        if (storage.getThemeMode() == themeMode) {
            return;
        }
        storage.setThemeMode(themeMode);
        if (isViewAttached()) {
            view.applyTheme(themeMode);
        }
    }

    /**
     * Сохраняем язык и просим экран обновиться.
     */
    @Override
    public void onLanguageSelected(String languageTag) {
        if (languageTag == null || languageTag.equals(storage.getLanguageTag())) {
            return;
        }
        storage.setLanguageTag(languageTag);
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
            storage.setUserName("");
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

        storage.setUserName(normalized);
        if (isViewAttached()) {
            view.clearUserNameError();
        }
    }

    /**
     * Сохраняем выбранный аватар и обновляем картинку на экране.
     */
    @Override
    public void onAvatarSelected(int avatarResId) {
        storage.setAvatarResId(avatarResId);
        if (isViewAttached()) {
            view.updateAvatarPreview(avatarResId);
        }
    }

    /**
     * Проверяем данные формы аккаунта и показываем результат.
     */
    @Override
    public void onCreateAccountClicked(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email == null ? "" : email.trim()).matches()) {
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

        storage.setAccountCreated(true);
        if (isViewAttached()) {
            view.showAccountCreatedState(true);
            view.showAccountCreatedMessage();
        }
    }

    /**
     * Сбрасываем локальный профиль и возвращаем экран к форме создания аккаунта.
     */
    @Override
    public void onLogoutClicked() {
        storage.clearLocalAccount();
        if (isViewAttached()) {
            view.showCurrentSettings(
                    storage.getThemeMode(),
                    storage.getLanguageTag(),
                    storage.getUserName(),
                    storage.getAvatarResId(),
                    storage.isAccountCreated()
            );
            view.showLoggedOutMessage();
        }
    }
}




