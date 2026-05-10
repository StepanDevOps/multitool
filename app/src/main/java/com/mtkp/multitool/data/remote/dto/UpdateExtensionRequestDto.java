package com.mtkp.multitool.data.remote.dto;

import java.util.List;

/**
 * Тело запроса для обновления карточки расширения владельцем.
 */
public class UpdateExtensionRequestDto {
    public String name;
    public String shortDescription;
    public String detailedDescription;
    public List<String> categories;

    public UpdateExtensionRequestDto(
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

