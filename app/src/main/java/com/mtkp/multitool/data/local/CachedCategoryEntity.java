package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Кешированная категория расширения из удалённого API.
 * Используется для оффлайн-доступа и быстрого отображения категорий в фильтрах.
 */
@Entity(tableName = "cached_categories")
public class CachedCategoryEntity {

    @PrimaryKey
    public int id;

    @NonNull
    public String name; // "utilities", "productivity"

    public String displayName; // "Утилиты", "Продуктивность"

    public String description;

    public CachedCategoryEntity() { }
}

