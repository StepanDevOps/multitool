package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Сущность InstalledExtension — связывает пользователя с установленными расширениями.
 * Хранит информацию об установленных у конкретного пользователя расширениях,
 * включая их версию, дату установки и данные о размещении на UI сетке.
 */
@Entity(
    tableName = "installed_extensions",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = ExtensionEntity.class,
            parentColumns = "id",
            childColumns = "extensionId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class InstalledExtensionEntity {
    
    /**
     * Уникальный идентификатор установленного расширения.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * ID пользователя, которому принадлежит это расширение.
     * Foreign Key к таблице users.
     */
    public int userId;
    
    /**
     * ID расширения из таблицы extensions.
     * Foreign Key к таблице extensions.
     */
    public int extensionId;
    
    /**
     * Установленная версия расширения.
     * Может отличаться от текущей версии в магазине (outdated extensions).
     */
    @NonNull
    public String installedVersion;
    
    /**
     * Дата установки расширения.
     */
    public long installedAt;
    
    /**
     * Включено ли расширение в данный момент.
     * true — расширение активно, false — деактивировано пользователем.
     */
    public boolean isEnabled;
    
    /**
     * Требуется ли обновление расширения.
     * true — доступна новая версия, false — актуальная версия.
     */
    public boolean needsUpdate;
    
    // === ПОЛЯ ДЛЯ UI СЕТКИ (Grid Layout) ===
    
    /**
     * Номер строки в GridLayout, где расположена карточка расширения.
     * 0-based индекс (0, 1, 2, ...)
     */
    public int gridRow;
    
    /**
     * Номер колонки в GridLayout.
     * 0-based индекс (0, 1, 2, ...)
     */
    public int gridColumn;
    
    /**
     * Ширина (span) карточки в количестве колонок сетки.
     * Пример: 1 — занимает одну колонку, 2 — две колонки и т.д.
     */
    public int gridSpanWidth;
    
    /**
     * Высота (span) карточки в количество рядов сетки.
     * Пример: 1 — занимает один ряд, 2 — два ряда и т.д.
     */
    public int gridSpanHeight;
    
    /**
     * Скрыта ли карточка расширения на главном экране.
     * true — удалена из видимости (но данные остаются, можно восстановить)
     * false — видима на главном экране
     */
    public boolean isHidden;
}

