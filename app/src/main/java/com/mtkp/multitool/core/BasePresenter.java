package com.mtkp.multitool.core;

/**
 * Базовый класс для всех презентеров.
 * T — это тип View, с которой будет работать презентер.
 */
public abstract class BasePresenter<T extends BaseView> {

    // Ссылка на экран (View), которой управляет этот презентер
    protected T view;

    // Метод для привязки экрана к презентеру
    public void attachView(T view) {
        this.view = view;
    }

    // Метод для отвязки (защита от утечек памяти)
    public void detachView() {
        this.view = null;
    }

    // Проверка: привязан ли сейчас экран?
    protected boolean isViewAttached() {
        return view != null;
    }
}