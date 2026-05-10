package com.mtkp.multitool.data.remote.dto;

import java.util.List;

/**
 * Обёртка списка отзывов, чтобы быть совместимыми с форматом { data: [...] }.
 */
public class RatingsResponseDto {
    public List<RatingDto> data;

    public RatingsResponseDto() {
    }
}

