package com.mtkp.multitool.data.remote.dto;

/**
 * Тело запроса на создание отзыва.
 */
public class CreateRatingRequestDto {
    public int rating;
    public String review;

    public CreateRatingRequestDto(int rating, String review) {
        this.rating = rating;
        this.review = review;
    }
}

