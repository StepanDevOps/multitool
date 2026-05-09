package com.mtkp.multitool.data.remote.dto;

/**
 * DTO пагинации для ответов списка.
 */
public class PaginationDto {
    public int page;
    public int per_page;
    public int total;
    public int total_pages;

    public PaginationDto() { }
}

