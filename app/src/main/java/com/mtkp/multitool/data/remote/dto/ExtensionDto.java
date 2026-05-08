package com.mtkp.multitool.data.remote.dto;

import java.util.List;

/**
 * DTO для расширения, получаемого из удалённого API.
 * Простая POJO-класс — используется как переносной объект между сетью и слоем данных.
 */
public class ExtensionDto {
    public int id;
    public String name;
    public String shortDescription;
    public String detailedDescription;
    public String authorName; // автор как строка для простоты
    public String logoUrl;
    public List<String> categories;
    public String version;
    public long downloads;
    public float rating;
    public String badge;
    public long updatedAt;

    // Пустой конструктор нужен для библиотек сериализации (Gson/Moshi)
    public ExtensionDto() { }
}

