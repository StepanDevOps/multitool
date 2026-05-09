package com.mtkp.multitool.data.remote.dto;

import java.util.List;

/**
 * Тело запроса создания расширения.
 */
public class CreateExtensionRequestDto {
    public String name;
    public String shortDescription;
    public String detailedDescription;
    public List<String> categories;

    public CreateExtensionRequestDto(
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories
    ) {
        this.name = name;
        this.shortDescription = shortDescription;
        this.detailedDescription = detailedDescription;
        this.categories = categories;
    }
}

