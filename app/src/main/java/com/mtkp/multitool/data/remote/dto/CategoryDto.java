package com.mtkp.multitool.data.remote.dto;

/**
 * DTO для категории расширения.
 */
public class CategoryDto {
    public int id;
    public String name;        // "utilities", "productivity", и т.д.
    public String displayName; // "Утилиты", "Продуктивность"
    public String description;

    public CategoryDto() { }
}

