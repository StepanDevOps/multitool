package com.mtkp.multitool.data.remote.dto;

/**
 * Тело запроса на обновление отзыва.
 */
public class UpdateRatingRequestDto {
    public Integer rating;
    public String review;

    public UpdateRatingRequestDto(Integer rating, String review) {
        this.rating = rating;
        this.review = review;
    }
}

