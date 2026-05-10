package com.mtkp.multitool.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Главный класс базы данных Room приложения MultiTool.
 *
 * AppDatabase — это центральная точка доступа к локальной БД.
 * Здесь регистрируются все Entity сущности и определяются абстрактные методы для получения DAO.
 *
 * Архитектура:
 * - Entity: описывают таблицы БД
 * - DAO: определяют операции над данными
 * - AppDatabase: объединяет Entity и DAO, управляет жизненным циклом БД
 */
@Database(
    entities = {
        InstalledExtensionEntity.class,
        LocalExtensionMetaEntity.class,
        SettingsEntity.class,
        com.mtkp.multitool.data.local.CachedExtensionEntity.class,
        com.mtkp.multitool.data.local.CachedCategoryEntity.class
    },
    version = 3,
    exportSchema = false  // false = не экспортировать schema в JSON для отладки
)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Получить DAO для работы с установленными расширениями.
     */
    public abstract InstalledExtensionDao installedExtensionDao();

    /**
     * Получить DAO для работы с локальными метаданными расширений.
     */
    public abstract LocalExtensionMetaDao localExtensionMetaDao();

    /**
     * Получить DAO для работы с настройками приложения.
     */
    public abstract SettingsDao settingsDao();

    // DAO для кеша расширений (локальный кэш удалённого каталога)
    public abstract CachedExtensionDao cachedExtensionDao();

    /**
     * Получить DAO для кеша категорий.
     */
    public abstract CachedCategoryDao cachedCategoryDao();

    // === СИНГЛТОН ПАТТЕРН ДЛЯ ИНИЦИАЛИЗАЦИИ БД ===

    /**
     * Единственный экземпляр AppDatabase в приложении.
     * Используется для предотвращения создания множественных подключений к БД.
     */
    private static AppDatabase instance;

    /**
     * Получить экземпляр базы данных (синглтон).
     * При первом вызове создаст БД, при остальных вернет существующий экземпляр.
     *
     * Параметры:
     * @param context — контекст приложения (Activity, Service, Application)
     *
     * @return единственный экземпляр AppDatabase
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Room.databaseBuilder создает конфигуратор для базы данных
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),  // используем Application контекст
                    AppDatabase.class,                // класс БД
                    "multitool_db"                    // имя файла БД
                )
                .fallbackToDestructiveMigration()     // локальная схема изменилась, пересоздаём БД при апгрейде
                .build();                             // собрать и создать БД
        }
        return instance;
    }
}
