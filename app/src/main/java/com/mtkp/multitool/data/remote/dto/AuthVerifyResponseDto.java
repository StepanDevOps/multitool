package com.mtkp.multitool.data.remote.dto;

/**
 * Ответ проверки токена.
 */
public class AuthVerifyResponseDto {
    public boolean valid;
    public long userId;
    public long expiresAt;

    public AuthVerifyResponseDto() { }
}

