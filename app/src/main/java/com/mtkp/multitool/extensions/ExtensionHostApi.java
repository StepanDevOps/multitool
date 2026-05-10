package com.mtkp.multitool.extensions;

/**
 * Безопасный API хоста для кода расширений.
 *
 * Плагин не должен напрямую менять внутренние структуры приложения.
 * Этот интерфейс даёт только ограниченный набор операций.
 */
public interface ExtensionHostApi {

    void log(String tag, String message);

    String getSetting(String key, String defaultValue);

    void putSetting(String key, String value);

    void removeSetting(String key);
}

