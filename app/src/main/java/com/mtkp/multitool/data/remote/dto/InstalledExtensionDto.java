package com.mtkp.multitool.data.remote.dto;

/**
 * DTO для установленного расширения.
 * Используется при получении списка установленных расширений пользователя.
 */
public class InstalledExtensionDto {
    public int id;
    public int extensionId;
    public String extensionName;
    public String installedVersion;
    public String currentAvailableVersion;
    public long installedAt;
    public boolean isEnabled;
    public boolean needsUpdate;

    public InstalledExtensionDto() { }
}

