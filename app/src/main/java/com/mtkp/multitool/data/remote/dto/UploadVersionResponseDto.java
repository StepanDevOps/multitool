package com.mtkp.multitool.data.remote.dto;

/**
 * Ответ на загрузку версии расширения.
 */
public class UploadVersionResponseDto {
    public int id;
    public int extensionId;
    public String version;
    public String releaseNotes;
    public long fileSize;
    public String sha256;
    public String downloadUrl;
    public long releasedAt;

    public UploadVersionResponseDto() { }
}

