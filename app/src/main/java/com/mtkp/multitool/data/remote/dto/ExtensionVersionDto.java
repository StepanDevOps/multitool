package com.mtkp.multitool.data.remote.dto;

/**
 * DTO для версии расширения.
 * Используется при получении информации об установленных расширениях и их версиях.
 */
public class ExtensionVersionDto {
    public int id;
    public int extensionId;
    public String version;
    public String releaseNotes;
    public long releasedAt;
    public long fileSize;
    public String downloadUrl;
    public String sha256;

    public ExtensionVersionDto() { }
}

