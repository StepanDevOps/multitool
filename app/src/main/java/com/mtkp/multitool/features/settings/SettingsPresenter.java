package com.mtkp.multitool.features.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.mtkp.multitool.core.BasePresenter;
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

    private static final int USERNAME_MIN_LEN = 3;
    private static final int USERNAME_MAX_LEN = 20;

    private final SettingsRepository repository;

    /**
     * Презентеру передаём контекст приложения.
     * Авторизация пока отключена, поэтому настройки храним как глобальные.
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
                repository.isAccountCreated()
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

        repository.setAccountCreated(true);
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
        repository.clearLocalAccount(); // Очищаем данные из model (метод из SettingsRepository)
        if (isViewAttached()) {
            view.showCurrentSettings(
                    repository.getThemeMode(),
                    repository.getLanguageTag(),
                    repository.getUserName(),
                    repository.getAvatarResId(),
                    repository.isAccountCreated()
            );
            view.showLoggedOutMessage();
        }
    }
}




