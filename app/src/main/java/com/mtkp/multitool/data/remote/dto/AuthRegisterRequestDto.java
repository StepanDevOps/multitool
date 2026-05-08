package com.mtkp.multitool.data.remote.dto;

/**
 * Тело запроса регистрации.
 */
public class AuthRegisterRequestDto {
    public String username;
    public String email;
    public String password;

    public AuthRegisterRequestDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

