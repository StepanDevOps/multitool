package com.mtkp.multitool.data.remote.dto;

/**
 * Тело запроса установки расширения пользователем на сервере.
 */
public class CreateInstalledExtensionRequestDto {
    public int extensionId;
    public String installedVersion;

    public CreateInstalledExtensionRequestDto(int extensionId, String installedVersion) {
        this.extensionId = extensionId;
        this.installedVersion = installedVersion;
    }
}

