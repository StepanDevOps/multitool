package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Сущность Settings — хранит пары ключ-значение для настроек приложения.
 *
 * Использует Key-Value паттерн для гибкого хранения различных настроек:
 * - Локальные настройки приложения (тема, язык и т.д.)
 *
 * Примеры ключей: "theme", "language", "notifications_enabled", "auto_update"
 */
@Entity(tableName = "settings")
public class SettingsEntity {
    /**
     * Ключ настройки — уникальный идентификатор параметра.
     * Примеры: "theme", "language", "auto_update", "notification_sound"
     */
    @PrimaryKey
    @NonNull
    public String key = "";

    /**
     * Значение настройки — может содержать любые строковые данные.
     * Примеры:
     * - Для "theme": "light", "dark", "system"
     * - Для "language": "ru", "en"
     * - Для "notifications_enabled": "true", "false"
     * - Для "auto_update": "always", "wifi_only", "never"
     */
    @NonNull
    public String value = "";

    /**
     * Дата создания/последнего изменения этой настройки.
     */
    public long lastUpdated;
}

