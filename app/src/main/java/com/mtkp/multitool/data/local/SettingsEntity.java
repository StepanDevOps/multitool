package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Сущность Settings — хранит пары ключ-значение для настроек приложения.
 *
 * Использует Key-Value паттерн для гибкого хранения различных настроек:
 * - Глобальные настройки (тема, язык и т.д.) — userId = null
 * - Персональные настройки пользователя — userId = конкретный ID
 *
 * Примеры ключей: "theme", "language", "notifications_enabled", "auto_update"
 */
@Entity(
    tableName = "settings",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class SettingsEntity {

    /**
     * Уникальный идентификатор настройки.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Ключ настройки — уникальный идентификатор параметра.
     * Примеры: "theme", "language", "auto_update", "notification_sound"
     */
    @NonNull
    public String key;

    /**
     * Значение настройки — может содержать любые строковые данные.
     * Примеры:
     * - Для "theme": "light", "dark", "system"
     * - Для "language": "ru", "en"
     * - Для "notifications_enabled": "true", "false"
     * - Для "auto_update": "always", "wifi_only", "never"
     */
    @NonNull
    public String value;

    /**
     * ID пользователя, к которому относится эта настройка.
     * Nullable — если null, то это глобальная настройка для всего приложения.
     * Если заполнен, то это персональная настройка конкретного пользователя.
     * Foreign Key к таблице users.
     */
    @Nullable
    public Integer userId;

    /**
     * Дата создания/последнего изменения этой настройки.
     */
    public long lastUpdated;
}

