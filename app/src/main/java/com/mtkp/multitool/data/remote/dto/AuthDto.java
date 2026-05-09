package com.mtkp.multitool.data.remote.dto;

/**
 * DTO для аутентификации.
 * Используется при регистрации и входе.
 */
public class AuthDto {
    public long id;
    public String username;
    public String email;
    public String token;
    public Long tokenExpiresAt;

    public AuthDto() { }
}

