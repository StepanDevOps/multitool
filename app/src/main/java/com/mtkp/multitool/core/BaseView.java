package com.mtkp.multitool.core;

/**
 * Базовый интерфейс для всех View (Activity/Fragment).
 * Сюда пишем то, что умеет каждый экран.
 */
public interface BaseView {
    /**
     * Все экраны должны уметь показывать ошибки.
     * Метод, служащий контрактом для этого.
     * @param message - сообщение с ошибкой.
     */
    void showError(String message);

    // Показать индикатор загрузки (крутилку)
    void showLoading();

    // Скрыть индикатор загрузки
    void hideLoading();
}