package com.mtkp.multitool.data.remote.dto;

/**
 * DTO отзыва/рейтинга расширения.
 */
public class RatingDto {
    public int id;
    public int extensionId;
    public int userId;
    public int rating;
    public String review;
    public long createdAt;

    public RatingDto() {
    }
}

