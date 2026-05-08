package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Кешированная версия расширения, получаемая с удалённого API.
 * Используется для отображения магазина оффлайн и быстрой загрузки списка.
 */
@Entity(tableName = "cached_extensions")
public class CachedExtensionEntity {

    @PrimaryKey
    public int id; // соответствует id на сервере

    @NonNull
    public String name;

    public String shortDescription;

    public String logoUrl;

    public String categoriesCsv; // простой список категорий через запятую

    public String version;

    public long downloads;

    public float rating;

    public String badge;

    public long updatedAt;

    public CachedExtensionEntity() { }
}

