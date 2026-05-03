package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Сущность Extension — представляет расширение в магазине.
 * Хранит информацию о доступных расширениях, которые можно установить.
 */
@Entity(
    tableName = "extensions",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "authorId",
            onDelete = ForeignKey.SET_NULL
        )
    }
)
public class ExtensionEntity {
    
    /**
     * Уникальный идентификатор расширения.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * Название расширения.
     */
    @NonNull
    public String name;
    
    /**
     * Краткое описание расширения для отображения на карточке в магазине.
     */
    @NonNull
    public String description;
    
    /**
     * Подробное описание на Markdown, для страницы расширения.
     * Может содержать форматирование, списки, заголовки и т.д.
     */
    @NonNull
    public String detailedDescription;
    
    /**
     * Автор расширения — ссылка на запись в таблице users (UserEntity.id).
     * Если расширение добавлено внешним источником и автор не зарегистрирован в приложении,
     * это поле может быть null.
     * 
     * Используем Integer (nullable) чтобы поддерживать поведение ForeignKey.SET_NULL
     * при удалении пользователя.
     */
    @Nullable
    public Integer authorId;
    
    /**
     * Текущая версия расширения в магазине.
     * Пример: "1.2.5"
     */
    @NonNull
    public String version;
    
    /**
     * Путь или URL логотипа расширения.
     * Пример: "https://example.com/logo.png" или локальный путь
     */
    @Nullable
    public String logoUrl;
    
    /**
     * Количество скачиваний/установок расширения.
     */
    public long downloads;
    
    /**
     * Средний рейтинг расширения от 0.0 до 5.0.
     * Пример: 4.5
     */
    public float rating;
    
    /**
     * Дата добавления расширения в магазин.
     */
    public long createdAt;
}

