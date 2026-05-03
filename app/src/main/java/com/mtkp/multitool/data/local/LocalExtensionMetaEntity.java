package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Сущность LocalExtensionMeta — технические метаданные скачанного расширения.
 * 
 * Хранит информацию о физическом .jar файле расширения на диске и его локальной БД.
 * Используется для управления файлами, проверки целостности, и взаимодействия с локальной БД расширения.
 * 
 * Архитектура:
 * - Каждое расширение — это отдельный .jar файл
 * - .jar файл может содержать свою локальную SQLite БД для хранения данных расширения
 * - Через API расширение может запрашивать данные из основной БД приложения
 */
@Entity(
    tableName = "local_extension_meta",
    foreignKeys = {
        @ForeignKey(
            entity = InstalledExtensionEntity.class,
            parentColumns = "id",
            childColumns = "installedExtensionId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class LocalExtensionMetaEntity {
    
    /**
     * Уникальный идентификатор метаданных.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * ID установленного расширения из таблицы installed_extensions.
     * Foreign Key к таблице installed_extensions.
     */
    public int installedExtensionId;
    
    /**
     * Полный путь к .jar файлу расширения на файловой системе.
     * Пример: "/data/data/com.mtkp.multitool/extensions/calculator_extension.jar"
     * Хранится для быстрого доступа к файлу без необходимости восстанавливать путь.
     */
    @NonNull
    public String jarFilePath;
    
    /**
     * Полный путь к локальной БД расширения (если она существует).
     * Пример: "/data/data/com.mtkp.multitool/extension_databases/calculator.db"
     * Nullable — не все расширения имеют локальную БД.
     */
    @Nullable
    public String localDatabasePath;
    
    /**
     * Размер .jar файла в байтах.
     * Помогает отслеживать используемое дисковое пространство.
     * Пример: 2048576 (2 МБ)
     */
    public long jarFileSize;
    
    /**
     * Хэш контрольной суммы .jar файла (SHA-256 или MD5).
     * Используется для проверки целостности файла после скачивания или обновления.
     * Пример: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
     */
    @NonNull
    public String jarFileHash;
    
    /**
     * Время последнего изменения .jar файла (мс с 1970 года).
     * Используется для отслеживания обновлений внешних файлов.
     */
    public long lastModified;
    
    /**
     * Флаг повреждения файла.
     * true — файл поврежден, расширение нужно переустановить
     * false — файл в порядке
     */
    public boolean isCorrupted;
}

