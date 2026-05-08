package com.mtkp.multitool.data.remote.dto;

/**
 * Тело запроса логина.
 */
public class AuthLoginRequestDto {
    public String email;
    public String password;

    public AuthLoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

