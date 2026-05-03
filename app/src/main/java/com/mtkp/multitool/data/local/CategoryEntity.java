package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Сущность Category — представляет категорию расширений.
 * Категории группируют расширения по типам (Утилиты, Продуктивность, Развлечения и т.д.).
 * Одно расширение может принадлежать нескольким категориям.
 */
@Entity(tableName = "categories")
public class CategoryEntity {
    
    /**
     * Уникальный идентификатор категории.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * Название категории.
     * Примеры: "Утилиты", "Продуктивность", "Развлечения", "Образование", "Коммуникация", "Социальные сети"
     */
    @NonNull
    public String name;
    
    /**
     * Путь или URL иконки категории для отображения в UI.
     * Пример: "@drawable/ic_category_utilities"
     * Nullable, так как можно использовать дефолтную иконку.
     */
    @Nullable
    public String iconUrl;
}

