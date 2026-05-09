package com.mtkp.multitool.data.remote.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mtkp.multitool.data.remote.dto.AuthDto;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тест десериализации ISO-8601 дат в поле tokenExpiresAt AuthDto.
 */
public class DateTimeDeserializerTest {

    @Test
    public void testDeserializeAuthDtoWithIso8601Date() {
        // Подготовка: создаём Gson с десериализатором, как в ApiClient
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Long.class, new DateTimeDeserializer())
                .create();

        // JSON ответ, как возвращает сервер
        String jsonResponse = "{\"id\":2," +
                "\"username\":\"stepanpeshkov101\"," +
                "\"email\":\"stepanpeshkov101@gmail.com\"," +
                "\"token\":\"eyJhbGciOiJIUzUxMiJ9...\"," +
                "\"tokenExpiresAt\":\"2026-05-10T19:14:25\"}";

        // Действие: десериализуем JSON в AuthDto
        AuthDto authDto = gson.fromJson(jsonResponse, AuthDto.class);

        // Проверка: убеждаемся, что tokenExpiresAt корректно десериализовано в миллисекунды
        assertNotNull("tokenExpiresAt не должен быть null", authDto.tokenExpiresAt);
        assertTrue("tokenExpiresAt должен быть положительным числом", authDto.tokenExpiresAt > 0);
        assertEquals("id должен быть 2", 2, authDto.id);
        assertEquals("username должен быть stepanpeshkov101", "stepanpeshkov101", authDto.username);
    }

    @Test
    public void testDeserializeAuthDtoWithNumericDate() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Long.class, new DateTimeDeserializer())
                .create();

        // JSON с числовым значением tokenExpiresAt (обратная совместимость)
        String jsonResponse = "{\"id\":1," +
                "\"username\":\"user1\"," +
                "\"email\":\"user1@example.com\"," +
                "\"token\":\"token123\"," +
                "\"tokenExpiresAt\":1747968865000}";

        AuthDto authDto = gson.fromJson(jsonResponse, AuthDto.class);

        assertEquals("tokenExpiresAt должен быть 1747968865000", 1747968865000L, (long) authDto.tokenExpiresAt);
    }

    @Test
    public void testDeserializeAuthDtoWithTimeZoneOffset() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Long.class, new DateTimeDeserializer())
                .create();

        // JSON с часовым поясом
        String jsonResponse = "{\"id\":3," +
                "\"username\":\"user3\"," +
                "\"email\":\"user3@example.com\"," +
                "\"token\":\"token456\"," +
                "\"tokenExpiresAt\":\"2026-05-10T19:14:25+03:00\"}";

        AuthDto authDto = gson.fromJson(jsonResponse, AuthDto.class);

        assertNotNull("tokenExpiresAt не должен быть null", authDto.tokenExpiresAt);
        assertTrue("tokenExpiresAt должен быть положительным числом", authDto.tokenExpiresAt > 0);
    }
}

