# Model Layer Documentation

## Структура слоя Model (data/)

Слой Model отвечает за управление данными — как локальными (Room SQLite), так и удалёнными (PostgreSQL через API).

### Пакеты и их назначение

#### 1. `data/remote/` — Удалённые данные (PostgreSQL через REST API)

**Содержит:**
- `ExtensionsApi.java` — интерфейс API (независимый от реализации сети)
- `RemoteDataSource.java` — реализация API (заглушка, будет заменена на Retrofit)
- `dto/` — классы передачи данных (DTO) между сетью и приложением

**DTO классы:**
- `ExtensionDto.java` — расширение из каталога и описание
- `CategoryDto.java` — категория расширения
- `ExtensionVersionDto.java` — версия расширения
- `InstalledExtensionDto.java` — информация об установке пользователем
- `AuthDto.java` — данные аутентификации (логин/регистрация)

**Когда использовать:**
- При получении данных с сервера эти классы преобразуются в Entity (через Mapper)
- Все DTO — это POJO с пустым конструктором (для JSON парсеров типа Gson)

#### 2. `data/local/` — Локальные данные (Room SQLite на устройстве)

**Entity классы (таблицы):**
- `SettingsEntity.java` — локальные настройки приложения (тема, язык и т.д.)
- `InstalledExtensionEntity.java` — установленные пользователем расширения + позиция на сетке
- `LocalExtensionMetaEntity.java` — метаданные (путь, хеш) скачанных .jar файлов
- `CachedExtensionEntity.java` — локальный кэш расширений из каталога (для оффлайн)
- `CachedCategoryEntity.java` — локальный кэш категорий (для оффлайн)

**DAO интерфейсы (методы доступа):**
- `SettingsDao.java` — операции над настройками
- `InstalledExtensionDao.java` — операции над установленными расширениями
- `LocalExtensionMetaDao.java` — операции над метаданными файлов
- `CachedExtensionDao.java` — операции над кешем расширений
- `CachedCategoryDao.java` — операции над кешем категорий

**AppDatabase.java — главная шлюза Room:**
```java
// Синглтон доступ к БД
AppDatabase db = AppDatabase.getInstance(context);
AppDatabase содержит методы для получения всех DAO
```

**Версионирование БД:** текущая версия = 4

#### 3. `data/mapper/` — Преобразование DTO → Entity

**DtoToEntityMapper.java:**
- `mapExtensionDtoToEntity(ExtensionDto)` → CachedExtensionEntity
- `mapCategoryDtoToEntity(CategoryDto)` → CachedCategoryEntity
- И их список-версии

**Зачем:**
- Отделяет логику трансформации от бизнес-логики
- Позволяет независимо менять DTO и Entity

#### 4. `data/repository/` — Бизнес-логика работы с данными

**ExtensionsRepository.java — главный репозиторий:**

Методы работы с расширениями:
```java
// Чтение локального кеша
List<CachedExtensionEntity> getCachedExtensions();
CachedExtensionEntity getCachedExtensionById(int id);

// Обновление кеша из сети (асинхронно)
void refreshExtensionsFromRemote(int page, int perPage);
```

Методы работы с категориями:
```java
List<CachedCategoryEntity> getCachedCategories();
void refreshCategoriesFromRemote();
```

Методы работы с установленными расширениями:
```java
List<InstalledExtensionEntity> getInstalledExtensions();
InstalledExtensionEntity getInstalledExtensionById(int id);
long installExtension(int extensionId, String version);
void uninstallExtension(int installedId);
void updateInstalledExtension(InstalledExtensionEntity entity);
```

Методы загрузки файлов:
```java
boolean downloadExtensionJar(int installedId, String name, String version, ...);
String getExtensionJarPath(int installedId);
boolean verifyExtensionFile(int installedId);
```

#### 5. `data/utils/` — Вспомогательные утилиты

**ExtensionDownloadManager.java:**
- Управление скачиванием .jar файлов
- Вычисление и проверка SHA256 хешей
- Сохранение метаданных файлов
- Удаление файлов при деинсталляции

**Методы:**
```java
boolean downloadExtension(int installedId, String extensionName, String version, ...);
String getExtensionJarPath(int installedId);
boolean verifyExtensionIntegrity(int installedId);
boolean deleteExtension(int installedId);
```

---

## Архитектура потока данных

```
┌─────────────────────────────────────────────────────────┐
│              UI Layer (Presenter/Activity)              │
│                                                         │
│  Получает данные из Repository и показывает на UI       │
└───────────────────────▲─────────────────────────────────┘
                        │
                        │ getCachedExtensions()
                        │ refreshFromRemote()
                        │
┌───────────────────────┴─────────────────────────────────┐
│         ExtensionsRepository (Репозиторий)              │
│                                                         │
│  Управляет локальным кешем и синхронизацией             │
│  - Читает из Room (быстро)                              │
│  - Обновляет из RemoteDataSource (в фоне)               │
│  - Управляет установками расширений                     │
└───────────┬─────────────────────────────────┬───────────┘
            │                                 │
            │ Room Queries                    │ Network Calls
            │                                 │
    ┌───────▼──────────┐          ┌───────────▼────────┐
    │  AppDatabase     │          │   RemoteDataSource │
    │  (LocalStorage)  │          │   (Network/REST)   │
    │                  │          │                    │
    │ Entities:        │          │ DTO Parsing:       │
    │ - Settings       │          │ - ExtensionDto     │
    │ - Installed...   │          │ - CategoryDto      │
    │ - Cache...       │          │ - etc              │
    │ - Local Meta...  │          │                    │
    └──────────────────┘          └────────────────────┘
```

---

## Использование в Presenter/Activity

### Пример 1: Загрузить список расширений

```java
// Сначала зачитываем кеш (быстро, может быть пусто)
new Thread(() -> {
    ExtensionsRepository repo = new ExtensionsRepository(context);
    List<CachedExtensionEntity> cached = repo.getCachedExtensions();
    
    // Показываем кеш на UI (или пусто, если первый запуск)
    runOnUiThread(() -> {
        adapter.setData(cached);
    });
}).start();

// Потом обновляем из сети (в фоне)
repo.refreshExtensionsFromRemote(1, 20); // page 1, 20 items per page
```

### Пример 2: Установить расширение

```java
ExtensionsRepository repo = new ExtensionsRepository(context);

// 1. Отметить как установленное локально
long installedId = repo.installExtension(extensionId, "1.0.0");

// 2. Скачать JAR в фоне
new Thread(() -> {
    boolean success = repo.downloadExtensionJar(
        installedId,
        "My Extension",
        "1.0.0",
        "https://api.../extensions/5/download",
        "sha256hash...",
        2048576
    );
    
    if (success) {
        // Можем теперь загрузить плагин
        String jarPath = repo.getExtensionJarPath(installedId);
        // loadPluginFromJar(jarPath);
    }
}).start();
```

---

## Совет: Интеграция с Retrofit (будущее)

Когда будет готов бэкенд, замените `RemoteDataSource.java`:

```java
public class RemoteDataSource implements ExtensionsApi {
    private final Retrofit retrofit;
    
    public RemoteDataSource() {
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor()) // JWT
            .build();
        
        retrofit = new Retrofit.Builder()
            .baseUrl("https://api.multitool.local/api/v1")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }
    
    @Override
    public List<ExtensionDto> fetchExtensions(int page, int perPage) throws Exception {
        return retrofit.create(ExtensionsApi.class)
            .fetchExtensions(page, perPage)
            .execute()
            .body();
    }
}
```

---

## Текущий статус

- [x] Entity и DAO для всех таблиц (Room)
- [x] DTO классы для всех API ответов
- [x] RemoteDataSource заглушка
- [x] ExtensionsRepository с полным функционалом
- [x] DtoToEntityMapper
- [x] ExtensionDownloadManager для .jar файлов
- [ ] Retrofit интеграция (будет реализовано при наличии API)
- [ ] LiveData/Flow для reactive подхода (опционально)
- [ ] WorkManager для фоновых синхронизаций (опционально)

---

## Best Practices (напоминание)

1. **Никогда не вызывай Room операции в main потоке** — всегда в фоне (Executor/Thread)
2. **AppDatabase синглтон** — используй `getInstance()` везде
3. **Mapper отделяет DTO от Entity** — так они не привязаны друг к другу
4. **RemoteDataSource часто обновляется** — запланируй использование Retrofit когда будет API
5. **SHA256 проверка обязательна** — для безопасности скачанных .jar файлов

