# MultiTool Extensions API Specification

## Общие сведения

**Базовый URL:** `https://api.multitool.local/api/v1`

**Версия API:** 1.0

**Формат данных:** JSON (UTF-8)

**Аутентификация:** JWT токен в заголовке `Authorization: Bearer <token>`

**Основные эндпойнты:**
* POST /auth/register, /auth/login — аутентификация
* GET /extensions — список с фильтрами/поиском/сортировкой
* GET /extensions/{id} — детали расширения и его версии
* PATCH /extensions/{id} — обновить карточку расширения (только владелец)
* GET /extensions/{id}/download — скачать JAR
* GET /categories — все категории
* POST/GET/DELETE /users/{userId}/installed-extensions — управление установками
* POST /extensions/{id}/versions — загрузить новую версию (для авторов)
* GET/POST/PATCH/DELETE /extensions/{id}/ratings — отзывы и оценки

---

## 1. Аутентификация

### 1.1 Регистрация пользователя
```
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Ответ (201 Created):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "token": "eyJhbGc...",
  "tokenExpiresAt": 1777000000000
}

Ошибки:
- 400 Bad Request: некорректные данные
- 409 Conflict: пользователь уже существует
```

### 1.2 Вход (логин)
```
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Ответ (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "token": "eyJhbGc...",
  "tokenExpiresAt": 1777000000000
}

Ошибки:
- 401 Unauthorized: неправильные учётные данные
- 400 Bad Request: некорректные данные
```

### 1.3 Проверка токена (опционально)
```
GET /auth/verify
Authorization: Bearer <token>

Ответ (200 OK):
{
  "valid": true,
  "userId": 1,
  "expiresAt": 1777000000000
}

Ошибки:
- 401 Unauthorized: токен невалиден или истёк
```

---

## 2. Управление расширениями (Extensions)

### 2.1 Получить список расширений
```
GET /extensions?page=1&per_page=20&category=utilities&sort=rating

Query параметры:
  - page: int (default: 1) — номер страницы для пагинации
  - per_page: int (default: 20) — количество элементов на странице
  - category: string (optional) — фильтр по категории (utilities, productivity, entertainment, etc.)
  - sort: string (default: updated_at) — сортировка (rating, downloads, updated_at, name)
  - search: string (optional) — поиск по названию/описанию

Ответ (200 OK):
{
  "data": [
    {
      "id": 1,
      "name": "Calculator",
      "shortDescription": "Advanced calculator with history",
      "detailedDescription": "# Calculator\nAdvanced calculator...",
      "authorId": 5,
      "authorName": "John Smith",
      "extensionPath": "/extensions/calculator.jar",
      "categories": ["utilities", "productivity"],
      "currentVersion": "2.1.0",
      "downloads": 15000,
      "rating": 4.8,
      "badge": "featured",
      "releasedAt": 1776500000000,
      "updatedAt": 1776900000000
    },
    ...
  ],
  "pagination": {
    "page": 1,
    "per_page": 20,
    "total": 150,
    "total_pages": 8
  }
}

Ошибки:
- 400 Bad Request: некорректные параметры
```

### 2.2 Получить расширение по ID
```
GET /extensions/{id}

Параметры пути:
  - id: int — ID расширения

Ответ (200 OK):
{
  "id": 1,
  "name": "Calculator",
  "shortDescription": "Advanced calculator with history",
  "detailedDescription": "# Calculator\nAdvanced calculator with...",
  "authorId": 5,
  "authorName": "John Smith",
  "extensionPath": "/extensions/calculator.jar",
  "categories": ["utilities", "productivity"],
  "currentVersion": "2.1.0",
  "downloads": 15000,
  "rating": 4.8,
  "badge": "featured",
  "releasedAt": 1776500000000,
  "updatedAt": 1776900000000,
  "versions": [
    {
      "version": "2.1.0",
      "releaseNotes": "Bug fixes and performance improvements",
      "releasedAt": 1776900000000,
      "fileSize": 2048576,
      "downloadUrl": "https://cdn.multitool.local/extensions/calculator/2.1.0/calculator.jar",
      "sha256": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
    },
    {
      "version": "2.0.5",
      "releaseNotes": "Initial release",
      "releasedAt": 1776500000000,
      "fileSize": 2000000,
      "downloadUrl": "https://cdn.multitool.local/extensions/calculator/2.0.5/calculator.jar",
      "sha256": "q1w2e3r4t5y6u7i8o9p0a1s2d3f4g5h6"
    }
  ]
}

Ошибки:
- 404 Not Found: расширение не найдено
```

### 2.3 Получить метаданные расширения (для проверки обновления)
```
GET /extensions/{id}/meta

Параметры пути:
  - id: int — ID расширения

Ответ (200 OK):
{
  "id": 1,
  "currentVersion": "2.1.0",
  "fileSize": 2048576,
  "sha256": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "downloadUrl": "https://cdn.multitool.local/extensions/calculator/2.1.0/calculator.jar",
  "releasedAt": 1776900000000,
  "badge": "featured"
}

Ошибки:
- 404 Not Found: расширение не найдено
```

---

## 3. Загрузка расширений (JAR файлы)

### 3.1 Скачать JAR расширения
```
GET /extensions/{id}/download?version=2.1.0

Параметры пути:
  - id: int — ID расширения

Query параметры:
  - version: string (optional) — конкретная версия (если не указана — текущая версия)

Ответ (200 OK):
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="calculator-2.1.0.jar"
Content-Length: 2048576
X-File-SHA256: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6

[Binary JAR file content]

Ошибки:
- 404 Not Found: расширение или версия не найдена
- 416 Range Not Supported: попытка скачать недопустимый диапазон
```

---

## 4. Категории расширений

### 4.1 Получить все доступные категории
```
GET /categories

Ответ (200 OK):
{
  "data": [
    {
      "id": 1,
      "name": "utilities",
      "displayName": "Утилиты",
      "description": "Полезные инструменты",
    },
    {
      "id": 2,
      "name": "productivity",
      "displayName": "Продуктивность",
      "description": "Инструменты для повышения производительности"
    },
    {
      "id": 3,
      "name": "entertainment",
      "displayName": "Развлечение",
      "description": "Игры и развлекательные приложения"
    },
    {
      "id": 4,
      "name": "weather",
      "displayName": "Погода",
      "description": "Прогноз и информация о погоде"
    },
    {
      "id": 5,
      "name": "health",
      "displayName": "Здоровье",
      "description": "Фитнес и здоровье"
    },
    {
      "id": 6,
      "name": "social",
      "displayName": "Социальные сети",
      "description": "Интеграции с соцсетями"
    }
  ]
}
```

---

## 5. Управление загруженными расширениями пользователя

### 5.1 Получить список установленных расширений пользователя
```
GET /users/{userId}/installed-extensions
Authorization: Bearer <token>

Параметры пути:
  - userId: int — ID пользователя

Ответ (200 OK):
{
  "data": [
    {
      "id": 1,
      "extensionId": 5,
      "extensionName": "Calculator",
      "installedVersion": "2.0.5",
      "currentAvailableVersion": "2.1.0",
      "installedAt": 1776000000000,
      "isEnabled": true,
      "needsUpdate": true
    },
    {
      "id": 2,
      "extensionId": 10,
      "extensionName": "Weather",
      "installedVersion": "1.5.0",
      "currentAvailableVersion": "1.5.0",
      "installedAt": 1775500000000,
      "isEnabled": true,
      "needsUpdate": false
    }
  ]
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 404 Not Found: пользователь не найден
```

### 5.2 Отметить расширение как установленное
```
POST /users/{userId}/installed-extensions
Authorization: Bearer <token>
Content-Type: application/json

{
  "extensionId": 5,
  "installedVersion": "2.1.0"
}

Ответ (201 Created):
{
  "id": 1,
  "extensionId": 5,
  "extensionName": "Calculator",
  "installedVersion": "2.1.0",
  "installedAt": 1776900000000,
  "isEnabled": true,
  "needsUpdate": false
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 404 Not Found: расширение не найдено
- 400 Bad Request: некорректные данные
```

### 5.3 Удалить установленное расширение
```
DELETE /users/{userId}/installed-extensions/{installedId}
Authorization: Bearer <token>

Параметры пути:
  - userId: int — ID пользователя
  - installedId: int — ID установленного расширения

Ответ (204 No Content):

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 404 Not Found: расширение не найдено
```

### 5.4 Обновить статус установленного расширения
```
PATCH /users/{userId}/installed-extensions/{installedId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "isEnabled": false,
  "installedVersion": "2.1.0"
}

Ответ (200 OK):
{
  "id": 1,
  "extensionId": 5,
  "extensionName": "Calculator",
  "installedVersion": "2.1.0",
  "currentAvailableVersion": "2.1.0",
  "installedAt": 1776000000000,
  "isEnabled": false,
  "needsUpdate": false
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 404 Not Found: расширение не найдено
```

---

## 6. Публикация расширений (для разработчиков)

### 6.1 Создать новое расширение (только авторизованные)
```
POST /extensions
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "My Calculator",
  "shortDescription": "A simple calculator",
  "detailedDescription": "# My Calculator\nDetails here...",
  "categories": ["utilities", "productivity"]
}

Ответ (201 Created):
{
  "id": 100,
  "name": "My Calculator",
  "shortDescription": "A simple calculator",
  "authorId": 1,
  "authorName": "John Doe",
  "extensionPath": null,
  "categories": ["utilities", "productivity"],
  "currentVersion": null,
  "downloads": 0,
  "rating": 0,
  "createdAt": 1776900000000
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 400 Bad Request: некорректные данные
- 409 Conflict: расширение с таким названием уже существует
```

### 6.2 Загрузить новую версию расширения
```
POST /extensions/{id}/versions
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
  - version: string (required) — версия (например, "1.0.0")
  - releaseNotes: string (required) — описание релиза
  - jarFile: binary (required) — JAR файл
  - changelog: string (optional) — подробный чейнджлог

Ответ (201 Created):
{
  "id": 1,
  "extensionId": 100,
  "version": "1.0.0",
  "releaseNotes": "Initial release",
  "fileSize": 2048576,
  "sha256": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "downloadUrl": "https://cdn.multitool.local/extensions/my-calculator/1.0.0/my-calculator.jar",
  "releasedAt": 1776900000000
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 404 Not Found: расширение не найдено
- 400 Bad Request: некорректные данные или неправильный JAR
- 413 Payload Too Large: файл слишком большой (макс. 100 МБ)
```

### 6.3 Обновить карточку расширения (только владелец)
```
PATCH /extensions/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "My Calculator Pro",
  "shortDescription": "A simple calculator with history",
  "detailedDescription": "# My Calculator Pro\nUpdated details...",
  "categories": ["utilities", "productivity"]
}

Ответ (200 OK):
{
  "id": 100,
  "name": "My Calculator Pro",
  "shortDescription": "A simple calculator with history",
  "detailedDescription": "# My Calculator Pro\nUpdated details...",
  "authorId": 1,
  "authorName": "John Doe",
  "categories": ["utilities", "productivity"],
  "currentVersion": "1.1.0",
  "downloads": 245,
  "rating": 4.6,
  "updatedAt": 1777000000000
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 403 Forbidden: только владелец расширения может редактировать карточку
- 404 Not Found: расширение не найдено
- 400 Bad Request: невалидные поля
```

---

## 7. Рейтинги и отзывы

### 7.1 Оставить рейтинг расширению
```
POST /extensions/{id}/ratings
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 5,
  "review": "Great extension!"
}

Ответ (201 Created):
{
  "id": 1,
  "userId": 1,
  "extensionId": 5,
  "rating": 5,
  "review": "Great extension!",
  "createdAt": 1776900000000
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 400 Bad Request: рейтинг должен быть от 1 до 5
```

### 7.2 Получить отзывы расширения
```
GET /extensions/{id}/ratings?page=1&per_page=20

Ответ (200 OK):
{
  "data": [
    {
      "id": 1,
      "userId": 1,
      "extensionId": 5,
      "rating": 5,
      "review": "Great extension!",
      "createdAt": 1776900000000
    }
  ]
}

Ошибки:
- 404 Not Found: расширение не найдено
```

### 7.3 Обновить собственный отзыв
```
PATCH /extensions/{id}/ratings/{ratingId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 4,
  "review": "After update works better"
}

Ответ (200 OK):
{
  "id": 1,
  "userId": 1,
  "extensionId": 5,
  "rating": 4,
  "review": "After update works better",
  "createdAt": 1776900000000
}

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 403 Forbidden: можно редактировать только свой отзыв
- 404 Not Found: отзыв не найден
```

### 7.4 Удалить собственный отзыв
```
DELETE /extensions/{id}/ratings/{ratingId}
Authorization: Bearer <token>

Ответ (204 No Content)

Ошибки:
- 401 Unauthorized: требуется аутентификация
- 403 Forbidden: можно удалять только свой отзыв
- 404 Not Found: отзыв не найден
```

---

## 8. Коды ошибок

| Код | Описание |
|-----|---------|
| 200 | OK — запрос успешно выполнен |
| 201 | Created — ресурс успешно создан |
| 204 | No Content — успешно, но нет содержимого в ответе |
| 400 | Bad Request — некорректные параметры запроса |
| 401 | Unauthorized — требуется аутентификация или токен невалиден |
| 403 | Forbidden — доступ запрещён |
| 404 | Not Found — ресурс не найден |
| 409 | Conflict — конфликт (например, дублирование) |
| 413 | Payload Too Large — размер файла слишком большой |
| 500 | Internal Server Error — ошибка сервера |

---

## 9. Примеры использования в клиентском коде

### Пример 1: Получить список расширений
```java
// Будет реализовано в RemoteDataSource
ExtensionsApi api = new RemoteDataSource();
List<ExtensionDto> extensions = api.fetchExtensions(1, 20); // page, perPage
```

### Пример 2: Скачать JAR файл
```java
// Будет реализовано в методе загрузки в repo
String downloadUrl = "https://api.multitool.local/api/v1/extensions/5/download?version=2.1.0";
// OkHttp/Retrofit с @Streaming будет загружать InputStream → File
```

### Пример 3: Проверка обновления расширения
```java
// Получить текущую версию и сравнить с installedVersion
// Если currentAvailableVersion > installedVersion → есть обновление
```

---

## 10. Примечания для разработчика бэкенда

1. **Версионирование API:** Используй `/api/v1/...` для всех эндпойнтов. Изменения версии указывают на breaking changes.
2. **JWT токены:** Используй RS256 или HS256 (выбери алгоритм при инициализации).
3. **Paging:** По умолчанию 20 элементов на странице, макс. 100.
4. **Сортировка:** Доступны: `rating`, `downloads`, `updated_at`, `name`, `-updated_at` (минус для сортировки по убыванию).
5. **Кэширование:** Клиент кэширует список расширений локально в Room. Сервер может использовать ETag для условной загрузки.
6. **CORS:** Не забудь разрешить запросы с мобильного клиента (localhost:5000 для разработки).
7. **HTTPS:** На продакшене всегда использовать HTTPS.

---

## 11. Фильтры и поиск (детали)

### Категории (доступные значения):
- `utilities` — Утилиты
- `productivity` — Продуктивность
- `entertainment` — Развлечение
- `weather` — Погода
- `health` — Здоровье
- `social` — Социальные сети

### Бейджи (возможные значения):
- `featured` — Избранное расширение
- `new` — Новое расширение
- `trending` — Популярное
- `premium` — Платное