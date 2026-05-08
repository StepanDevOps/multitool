package com.mtkp.multitool.data.remote.dto;

import java.util.List;

/**
 * Ответ API для списка расширений.
 */
public class ExtensionsListResponseDto {
    public List<ExtensionDto> data;
    public PaginationDto pagination;

    public ExtensionsListResponseDto() { }
}

