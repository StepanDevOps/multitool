# Документация для разработчиков расширений (Plugin API)

Этот документ описывает текущее состояние интеграции плагинов в `MultiTOOL` и как разработчику подготовить/загрузить расширение.

## 1. Что уже поддерживается

- Каталог расширений загружается из backend API (`GET /extensions`).
- Скачивание бинарника расширения поддерживается (`GET /extensions/{id}/download?version=...`).
- Проверка целостности через SHA-256 поддерживается (если `sha256` приходит из `GET /extensions/{id}/meta`).
- Runtime-загрузка плагина через `DexClassLoader` поддерживается.
- Базовый lifecycle плагина поддерживается: `onLoad(...)` / `onUnload()`.
- Публикация расширения из приложения поддержана на уровне методов:
  - создание карточки расширения (`POST /extensions`)
  - загрузка версии (`POST /extensions/{id}/versions`, multipart).

## 2. Что пока НЕ поддерживается (важно)

- Публикация через полноценный UI-поток в приложении (есть методы, но экран загрузки автора пока не завершен).
- Оставление отзывов/оценок из Android-клиента (нет endpoint-методов в текущем `BackendApiService`).
- Изменение уже созданного описания расширения (нет endpoint типа `PATCH /extensions/{id}` в клиенте).

## 3. Контракт плагина (обязательный)

Плагин должен реализовать интерфейс:

- `com.mtkp.multitool.extensions.ExtensionInterface`

Минимум обязательных методов:

- `String getExtensionId()`
- `String getDisplayName()`
- `int getRequiredApiVersion()`
- `void onLoad(Context context, ExtensionHostApi hostApi)`
- `void onUnload()`

Текущая версия API хоста:

- `ExtensionInterface.HOST_API_VERSION = 1`

Если `getRequiredApiVersion()` у плагина больше, чем версия хоста, загрузка будет отклонена.

## 4. Точка входа (entry class)

По умолчанию `ExtensionManager` ищет класс:

- `com.mtkp.multitool.plugin.PluginEntry`

Этот класс должен:

- иметь публичный конструктор без аргументов
- реализовывать `ExtensionInterface`

## 5. Пример каркаса плагина

```java
package com.mtkp.multitool.plugin;

import android.content.Context;
import com.mtkp.multitool.extensions.ExtensionHostApi;
import com.mtkp.multitool.extensions.ExtensionInterface;

public class PluginEntry implements ExtensionInterface {

    @Override
    public String getExtensionId() {
        return "my_plugin";
    }

    @Override
    public String getDisplayName() {
        return "My Plugin";
    }

    @Override
    public int getRequiredApiVersion() {
        return HOST_API_VERSION;
    }

    @Override
    public void onLoad(Context context, ExtensionHostApi hostApi) {
        hostApi.log("MyPlugin", "Plugin loaded");
    }

    @Override
    public void onUnload() {
        // cleanup
    }
}
```

## 6. Runtime API, доступный плагину

Через `ExtensionHostApi` плагин может:

- писать логи (`log`)
- читать/писать свои key-value настройки (`getSetting`, `putSetting`, `removeSetting`)

`ExtensionHostApi` намеренно ограничен для безопасности.

## 7. Публикация расширения (текущий flow)

На уровне клиентских методов доступно:

1. Создать расширение:
   - `ExtensionDeveloperApi.createExtension(name, shortDescription, detailedDescription, categories, callback)`
2. Загрузить бинарник версии:
   - `ExtensionDeveloperApi.uploadVersion(extensionId, version, releaseNotes, jarFile, changelog, callback)`

После публикации версия должна быть доступна через:

- `GET /extensions/{id}/meta`
- `GET /extensions/{id}/download?version=...`

## 8. Markdown в описаниях

- Краткое и подробное описание передаются как обычные строки (`shortDescription`, `detailedDescription`).
- На экране расширения применяется упрощенный markdown-рендер (`SimpleMarkdownFormatter`) для подробного текста.
- Обновление описаний после создания в текущем Android API не реализовано (нужен backend endpoint и клиентский метод обновления).

## 9. Соответствие PostgreSQL-схеме (кратко)

С учётом таблиц `extensions`, `extension_versions`, `categories`, `extension_category_cross_ref`, `ratings`:

- Создание расширения и загрузка версий покрывают `extensions` + `extension_versions`.
- Категории ожидаются в `createExtension(..., categories)` и должны маппиться backend-ом в `categories` / `extension_category_cross_ref`.
- Для `ratings` в Android-клиенте пока нет методов (оставление отзывов недоступно).

## 10. Что нужно добавить для полного developer UX

- Endpoint и клиентский метод обновления расширения (`PATCH /extensions/{id}`) для редактирования `shortDescription`/`detailedDescription`.
- Endpoint-ы рейтингов:
  - `POST /extensions/{id}/ratings`
  - `GET /extensions/{id}/ratings`
  - опционально `PATCH/DELETE` для собственных отзывов.
- Полноценный экран публикации расширения в приложении (без ручного вызова API-методов).

