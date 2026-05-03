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
        UserEntity.class,
        ExtensionEntity.class,
        InstalledExtensionEntity.class,
        CategoryEntity.class,
        ExtensionCategoryCrossRef.class,
        ExtensionVersionEntity.class,
        LocalExtensionMetaEntity.class,
        SettingsEntity.class
    },
    version = 1,
    exportSchema = false  // false = не экспортировать schema в JSON для отладки
)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Получить DAO для работы с пользователями.
     * Позже здесь будет UserDao с методами insert, update, delete, query и т.д.
     */
    // public abstract UserDao userDao();

    /**
     * Получить DAO для работы с расширениями из магазина.
     */
    // public abstract ExtensionDao extensionDao();

    /**
     * Получить DAO для работы с установленными расширениями пользователя.
     */
    // public abstract InstalledExtensionDao installedExtensionDao();

    /**
     * Получить DAO для работы с категориями.
     */
    // public abstract CategoryDao categoryDao();

    /**
     * Получить DAO для работы со связми Extension-Category.
     */
    // public abstract ExtensionCategoryCrossRefDao extensionCategoryCrossRefDao();

    /**
     * Получить DAO для работы с версиями расширений.
     */
    // public abstract ExtensionVersionDao extensionVersionDao();

    /**
     * Получить DAO для работы с локальными метаданными расширений.
     */
    // public abstract LocalExtensionMetaDao localExtensionMetaDao();

    /**
     * Получить DAO для работы с настройками приложения.
     */
    // public abstract SettingsDao settingsDao();

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
                .build();                             // собрать и создать БД
        }
        return instance;
    }
}
