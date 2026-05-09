package com.mtkp.multitool.features.settings;

import com.mtkp.multitool.core.BaseView;

/**
 * Контракт экрана настроек.
 * В MVP-контракте мы заранее описываем, что умеет View,
 * и какие действия может вызывать Presenter.
 * Это помогает держать логику экрана аккуратной и понятной.
 */
public interface SettingsContract {

    /**
     * Интерфейс представления (экрана).
     * Сюда входят только методы, которые нужны для обновления интерфейса,
     * показа ошибок и отображения текущих данных на экране.
     */
    interface View extends BaseView {
        /**
         * Показать на экране уже сохранённые настройки.
         * Вызывается, когда экран только открылся.
         */
        void showCurrentSettings(int themeMode, String languageTag, String userName,
                                 int avatarResId, boolean accountCreated);

        /**
         * Применить выбранную тему оформления к приложению.
         */
        void applyTheme(int themeMode);

        /**
         * Применить выбранный язык интерфейса ко всему приложению.
         */
        void applyLanguage(String languageTag);

        /**
         * Показать ошибку, если имя пользователя не проходит проверку.
         */
        void showUserNameError();

        /**
         * Скрыть сообщение об ошибке для имени пользователя.
         */
        void clearUserNameError();

        /**
         * Обновить картинку аватара на экране настроек.
         */
        void updateAvatarPreview(int avatarResId);

        /**
         * Показать состояние: создан ли локальный аккаунт или нет.
         */
        void showAccountCreatedState(boolean accountCreated);

        /**
         * Показать ошибку, если введён неверный email.
         */
        void showInvalidEmailError();

        /**
         * Показать ошибку, если пароль слишком короткий.
         */
        void showWeakPasswordError();

        /**
         * Показать ошибку, если пароли не совпали.
         */
        void showPasswordMismatchError();

        /**
         * Показать сообщение об успешной регистрации аккаунта на сервере.
         */
        void showAccountCreatedMessage();

        /**
         * Показать сообщение об успешном входе в аккаунт.
         */
        void showLoggedInMessage();

        /**
         * Показать сообщение об успешном выходе из локального профиля.
         */
        void showLoggedOutMessage();

        /**
         * Показать обычное текстовое сообщение пользователю.
         */
        void showMessage(String message);

    }

    /**
     * Интерфейс презентера.
     * Presenter описывает только бизнес-логику экрана — методы, которые
     * обрабатывают действия пользователя и взаимодействуют с репозиторием.
     *
     * Примечание: lifecycle-методы (attachView/detachView) реализованы в
     * базовом классе BasePresenter и поэтому НЕ объявляются в контракте.
     */
    interface Presenter {

        /**
         * Загрузить сохранённые настройки и показать их на экране.
         */
        void loadSettings();

        /**
         * Обработать выбор темы пользователем.
         */
        void onThemeSelected(int themeMode);

        /**
         * Обработать выбор языка пользователем.
         */
        void onLanguageSelected(String languageTag);

        /**
         * Обработать изменение имени пользователя.
         */
        void onUserNameChanged(String userName);

        /**
         * Обработать выбор аватара.
         */
        void onAvatarSelected(int avatarResId);

        /**
         * Обработать попытку регистрации аккаунта на сервере.
         */
        void onCreateAccountClicked(String userName, String email, String password, String confirmPassword);

        /**
         * Обработать попытку входа в аккаунт на сервере.
         */
        void onLoginClicked(String email, String password);

        /**
         * Обработать выход из локального профиля.
         */
        void onLogoutClicked();
    }
}






