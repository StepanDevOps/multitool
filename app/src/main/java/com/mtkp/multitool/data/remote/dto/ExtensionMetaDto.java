package com.mtkp.multitool.data.remote.dto;

/**
 * Метаданные расширения (для проверки обновлений/скачивания).
 */
public class ExtensionMetaDto {
    public int id;
    public String currentVersion;
    public long fileSize;
    public String sha256;
    public String downloadUrl;
    public long releasedAt;
    public String badge;

    public ExtensionMetaDto() { }
}

