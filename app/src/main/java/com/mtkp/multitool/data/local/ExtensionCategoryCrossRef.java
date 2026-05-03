package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.annotation.NonNull;

/**
 * CrossRef (Cross Reference) таблица — связь много-ко-многим между Extensions и Categories.
 * 
 * Зачем это нужно:
 * Одно расширение может принадлежать нескольким категориям одновременно.
 * Например, расширение "Заметки" может быть одновременно в категориях "Продуктивность" и "Образование".
 * 
 * Вместо хранения массива categoryId в ExtensionEntity, мы создаем отдельную таблицу,
 * где каждая строка представляет одну связь между расширением и категорией.
 */
@Entity(
    tableName = "extension_category_cross_ref",
    primaryKeys = {"extensionId", "categoryId"},  // Композитный первичный ключ
    foreignKeys = {
        @ForeignKey(
            entity = ExtensionEntity.class,
            parentColumns = "id",
            childColumns = "extensionId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = CategoryEntity.class,
            parentColumns = "id",
            childColumns = "categoryId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class ExtensionCategoryCrossRef {
    
    /**
     * ID расширения.
     * Foreign Key к таблице extensions.
     * Часть композитного первичного ключа.
     */
    public int extensionId;
    
    /**
     * ID категории.
     * Foreign Key к таблице categories.
     * Часть композитного первичного ключа.
     */
    public int categoryId;
}

