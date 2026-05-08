package com.mtkp.multitool.data.remote.dto;

/**
 * Тело PATCH-запроса обновления установленного расширения.
 */
public class UpdateInstalledExtensionRequestDto {
    public Boolean isEnabled;
    public String installedVersion;

    public UpdateInstalledExtensionRequestDto(Boolean isEnabled, String installedVersion) {
        this.isEnabled = isEnabled;
        this.installedVersion = installedVersion;
    }
}

