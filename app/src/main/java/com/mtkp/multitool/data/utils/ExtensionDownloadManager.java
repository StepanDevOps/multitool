package com.mtkp.multitool.data.utils;

import android.content.Context;

import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.LocalExtensionMetaDao;
import com.mtkp.multitool.data.local.LocalExtensionMetaEntity;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Утилита для скачивания и проверки целостности JAR-файлов расширений.
 *
 * Ответственна за:
 * - Скачивание файла с сервера
 * - Вычисление SHA256 хеша
 * - Сравнение с эталонным хешем с сервера
 * - Сохранение метаданных в LocalExtensionMetaEntity
 */
public class ExtensionDownloadManager {

    private final LocalExtensionMetaDao metaDao;
    private final Context context;
    private static final String EXTENSIONS_DIR = "extensions";

    public ExtensionDownloadManager(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.metaDao = db.localExtensionMetaDao();
    }

    /**
     * Скачать JAR-файл расширения и сохранить метаданные.
     *
     * @param installedExtensionId ID установленного расширения
     * @param extensionName название расширения (для имени файла)
     * @param version версия
     * @param downloadUrl URL для скачивания (например, из ExtensionVersionDto)
     * @param expectedSha256 ожидаемый SHA256 из сервера
     * @param fileSize размер файла в байтах
     * @return успешность загрузки
     *
     * TODO: реализовать скачивание через OkHttp/Retrofit
     * - Использовать @Streaming аннотацию для больших файлов
     * - Писать в файл блоками (BufferedInputStream/FileOutputStream)
     * - По завершении вычислить SHA256
     */
    public boolean downloadExtension(
            int installedExtensionId,
            String extensionName,
            String version,
            String downloadUrl,
            String expectedSha256,
            long fileSize) {

        try {
            // TODO: скачивание файла через сеть
            // File jarFile = downloadFromUrl(downloadUrl);

            // Пока заглушка — просто создаём пустой файл для тестирования
            File extensionsDir = new File(context.getFilesDir(), EXTENSIONS_DIR);
            if (!extensionsDir.exists()) {
                extensionsDir.mkdirs();
            }

            String fileName = extensionName + "-" + version + ".jar";
            File jarFile = new File(extensionsDir, fileName);

            // Если файл не существует — создаём пустой (заглушка)
            if (!jarFile.exists()) {
                jarFile.createNewFile();
            }

            // Вычисляем SHA256
            String actualSha256 = calculateSha256(jarFile);

            // Проверяем хеш
            if (!actualSha256.equals(expectedSha256)) {
                // TODO: логирование: хеши не совпадают
                jarFile.delete(); // удалить повреждённый файл
                return false;
            }

            // Сохраняем метаданные в БД
            LocalExtensionMetaEntity meta = new LocalExtensionMetaEntity();
            meta.installedExtensionId = installedExtensionId;
            meta.jarFilePath = jarFile.getAbsolutePath();
            meta.localDatabasePath = null; // пока не используется
            meta.jarFileSize = fileSize;
            meta.jarFileHash = actualSha256;
            meta.lastModified = System.currentTimeMillis();
            meta.isCorrupted = false;

            metaDao.insert(meta);
            return true;

        } catch (Exception e) {
            // TODO: логирование ошибки
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Вычислить SHA256 хеш файла.
     */
    private String calculateSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        byte[] hashBytes = digest.digest(fileBytes);
        return bytesToHex(hashBytes);
    }

    /**
     * Преобразовать массив байт в шестнадцатеричную строку.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Получить локальный путь к JAR-файлу расширения.
     */
    public String getExtensionJarPath(int installedExtensionId) {
        LocalExtensionMetaEntity meta = metaDao.getByInstalledId(installedExtensionId);
        return meta != null ? meta.jarFilePath : null;
    }

    /**
     * Проверить целостность файла расширения.
     */
    public boolean verifyExtensionIntegrity(int installedExtensionId) {
        LocalExtensionMetaEntity meta = metaDao.getByInstalledId(installedExtensionId);
        if (meta == null || meta.isCorrupted) {
            return false;
        }

        try {
            File jarFile = new File(meta.jarFilePath);
            if (!jarFile.exists()) {
                return false;
            }

            String actualHash = calculateSha256(jarFile);
            return actualHash.equals(meta.jarFileHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Удалить JAR-файл расширения.
     */
    public boolean deleteExtension(int installedExtensionId) {
        LocalExtensionMetaEntity meta = metaDao.getByInstalledId(installedExtensionId);
        if (meta != null) {
            File jarFile = new File(meta.jarFilePath);
            if (jarFile.exists()) {
                jarFile.delete();
            }
            metaDao.delete(meta);
            return true;
        }
        return false;
    }
}


