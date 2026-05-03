package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Сущность ExtensionVersion — история версий расширений.
 * Отслеживает все выпущенные версии каждого расширения с датами и описанием изменений.
 * 
 * Используется для:
 * - Отображения истории обновлений
 * - Проверки доступных версий для установки
 * - Отката на предыдущую версию (если нужно)
 * - Уведомления пользователя о новых версиях
 */
@Entity(
    tableName = "extension_versions",
    foreignKeys = {
        @ForeignKey(
            entity = ExtensionEntity.class,
            parentColumns = "id",
            childColumns = "extensionId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class ExtensionVersionEntity {
    
    /**
     * Уникальный идентификатор версии.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * ID расширения, к которому относится эта версия.
     * Foreign Key к таблице extensions.
     */
    public int extensionId;
    
    /**
     * Номер версии расширения.
     * Пример: "1.0.0", "1.2.5", "2.0.0"
     * Не может быть null.
     */
    @NonNull
    public String versionNumber;
    
    /**
     * Описание изменений в этой версии (Release Notes/Changelog).
     * Пример: "- Исправлены ошибки в интерфейсе\n- Добавлена новая функция поиска"
     * Может быть пустой строкой если нет описания.
     */
    @NonNull
    public String releaseNotes;
    
    /**
     * Дата выпуска этой версии.
     * System.currentTimeMillis() при создании новой версии.
     */
    public long releasedAt;
    
    /**
     * Размер скачивания расширения в байтах.
     * Помогает пользователю оценить, сколько памяти займет расширение.
     */
    public long fileSize;
    
    /**
     * Это ли текущая (последняя) версия расширения в магазине.
     * true — это последняя версия, false — это старая архивная версия.
     */
    public boolean isCurrent;
}

