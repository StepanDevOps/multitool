package com.mtkp.multitool.data.remote.deserializer;

import android.annotation.SuppressLint;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom Gson deserializer для преобразования ISO-8601 строк в миллисекунды.
 *
 * Преобразует строки вида "2026-05-10T19:03:41" или "2026-05-10T19:03:41+03:00" в Long (миллисекунды с 1970).
 * Использует SimpleDateFormat для совместимости с Android API 24+.
 */
public class DateTimeDeserializer implements JsonDeserializer<Long> {


    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.isJsonPrimitive()) {
            // Проверим являться ли это числом или строкой
            if (json.getAsJsonPrimitive().isNumber()) {
                // Это число - просто вернём его как миллисекунды
                return json.getAsLong();
            }

            // Это строка - попробуем распарсить как ISO-8601 дату
            String dateString = json.getAsString();

            try {
                // Попытка распарсить различные форматы ISO-8601
                return parseIso8601(dateString);
            } catch (Exception e) {
                throw new JsonParseException(
                    "Cannot deserialize date string: " + dateString +
                    ". Expected ISO-8601 format (e.g., '2026-05-10T19:03:41' or '2026-05-10T19:03:41+03:00')",
                    e
                );
            }
        } else if (json.isJsonNull()) {
            return null;
        } else {
            throw new JsonParseException("Expected string or number for date field, got: " + json);
        }
    }

     /**
      * Распарсивает ISO-8601 строку в миллисекунды.
      * Поддерживает форматы:
      * - "2026-05-10T19:03:41"
      * - "2026-05-10T19:03:41Z"
      * - "2026-05-10T19:03:41+03:00"
      * - "2026-05-10T19:03:41.123"
      * - "2026-05-10T19:03:41.123Z"
      * - "2026-05-10T19:03:41.123+03:00"
      */
     @SuppressLint("SimpleDateFormat")
     private Long parseIso8601(String dateString) throws Exception {
         // Нормализуем формат
         dateString = dateString.trim();

         // Обработаем случаи с часовым поясом
         String timezonePart = "";
         String dateTimePart = dateString;

         // Извлечём часовой пояс если он есть
         if (dateString.endsWith("Z")) {
             dateTimePart = dateString.substring(0, dateString.length() - 1);
             timezonePart = "Z";
         } else if (dateString.contains("+") || (dateString.lastIndexOf("-") > 10)) {
             // Ищем + или - в конце (для +03:00 или -05:00)
             int tzIndex = Math.max(dateString.lastIndexOf("+"), dateString.lastIndexOf("-"));
             if (tzIndex > 10) {
                 timezonePart = dateString.substring(tzIndex);
                 dateTimePart = dateString.substring(0, tzIndex);
             }
         }

         // Парсим дату и время
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

         // Обработаем миллисекунды если они есть
         if (dateTimePart.contains(".")) {
             String[] parts = dateTimePart.split("\\.");
             dateTimePart = parts[0];
             // Миллисекунды в ISO-8601 могут быть разной длины (от 1 до 9 цифр)
             // SimpleDateFormat ожидает ровно 3 цифры
             String millis = parts[1];
             if (millis.length() < 3) {
                 millis = String.format("%-3s", millis).replace(' ', '0');
             } else if (millis.length() > 3) {
                 millis = millis.substring(0, 3);
             }
             dateTimePart = dateTimePart + "." + millis;
             sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
             sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         }

         // Парсим базовый формат без часового пояса (в UTC)
         Date date = sdf.parse(dateTimePart);
         long millis = date.getTime();

         // Если есть часовой пояс, обработаем его
         if (!timezonePart.isEmpty() && !timezonePart.equals("Z")) {
             // Парсим смещение часового пояса (например, "+03:00" или "-05:00")
             int sign = timezonePart.charAt(0) == '+' ? 1 : -1;
             String[] tzParts = timezonePart.substring(1).split(":");
             int hours = Integer.parseInt(tzParts[0]);
             int minutes = tzParts.length > 1 ? Integer.parseInt(tzParts[1]) : 0;
             long tzOffsetMillis = sign * (hours * 3600000L + minutes * 60000L);
             // Вычтём смещение, так как дата была распарсена в UTC,
             // но сервер отправил её с учётом часового пояса
             millis = millis - tzOffsetMillis;
         }

         return millis;
     }
 }


