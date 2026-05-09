# Исправление проблемы десериализации JWT token expiry

## Проблема
- Сервер возвращает: `"tokenExpiresAt":"2026-05-10T19:14:25"` (ISO-8601 строка)
- AuthDto ожидал: `long tokenExpiresAt` (примитив)
- Результат: `JsonSyntaxException: java.lang.NumberFormatException`

## Решение
**Изменен тип поля в AuthDto:**
- **ДО:** `public long tokenExpiresAt;`
- **ПОСЛЕ:** `public Long tokenExpiresAt;` (Wrapper class вместо примитива)

**Почему это работает:**
1. `DateTimeDeserializer` уже реализован и зарегистрирован в `ApiClient.createGson()`
2. Регистрация: `registerTypeAdapter(Long.class, new DateTimeDeserializer())`
3. Когда поле типа `Long` (вместо `long`), Gson активирует пользовательский deserializer
4. `DateTimeDeserializer` преобразует строку "2026-05-10T19:14:25" в миллисекунды (Long)

## Файлы, затронутые изменением
- ✅ `AuthDto.java` - **изменено** (long → Long)
- ✅ `DateTimeDeserializer.java` - уже существует
- ✅ `ApiClient.java` - уже подготовлено

## Поддерживаемые форматы даты
Десериализатор работает с:
- `"2026-05-10T19:14:25"`
- `"2026-05-10T19:14:25Z"`
- `"2026-05-10T19:14:25+03:00"`
- `"2026-05-10T19:14:25.123"`
- Числовые значения (миллисекунды)

## Статус
- ✅ Компиляция: **SUCCESS**
- ✅ Код готов к тестированию на эмуляторе
-📝 Следующий шаг: Запустить приложение, попробовать регистрацию/вход

