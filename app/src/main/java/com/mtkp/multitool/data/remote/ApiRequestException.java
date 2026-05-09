package com.mtkp.multitool.data.remote;

/**
 * Исключение для HTTP-ошибок API.
 * Нужен, чтобы репозиторий мог отличать, например, 401 от прочих ошибок.
 */
public class ApiRequestException extends RuntimeException {

    private final int code;

    public ApiRequestException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

