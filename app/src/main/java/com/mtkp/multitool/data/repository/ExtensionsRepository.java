package com.mtkp.multitool.data.repository;

import android.content.Context;

import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.CachedCategoryDao;
import com.mtkp.multitool.data.local.CachedCategoryEntity;
import com.mtkp.multitool.data.local.CachedExtensionDao;
import com.mtkp.multitool.data.local.CachedExtensionEntity;
import com.mtkp.multitool.data.local.InstalledExtensionDao;
import com.mtkp.multitool.data.local.InstalledExtensionEntity;
import com.mtkp.multitool.data.mapper.DtoToEntityMapper;
import com.mtkp.multitool.data.remote.ExtensionsApi;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.CategoryDto;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.utils.ExtensionDownloadManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Полный репозиторий для работы с каталогом расширений.
 *
 * Ответственен за:
 * - Управление локальным кешем (cached_extensions, cached_categories)
 * - Синхронизацию с удалённым сервером (RemoteDataSource)
 * - Загрузку и управление установленными расширениями
 * - Проверку целостности файлов
 *
 * Архитектура:
 * UI (Presenter) → ExtensionsRepository → {Local (Room) + Remote (RemoteDataSource)}
 */
public class ExtensionsRepository {

    private final CachedExtensionDao cachedExtensionDao;
    private final CachedCategoryDao cachedCategoryDao;
    private final InstalledExtensionDao installedExtensionDao;
    private final ExtensionsApi remote;
    private final ExtensionDownloadManager downloadManager;
    private final ExecutorService executor;

    public ExtensionsRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.cachedExtensionDao = db.cachedExtensionDao();
        this.cachedCategoryDao = db.cachedCategoryDao();
        this.installedExtensionDao = db.installedExtensionDao();
        this.remote = new RemoteDataSource(context);
        this.downloadManager = new ExtensionDownloadManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ===== РАСШИРЕНИЯ =====

    /**
     * Получить список всех кешированных расширений (синхронно).
     * Рекомендуется вызывать из фонового потока.
     */
    public List<CachedExtensionEntity> getCachedExtensions() {
        return cachedExtensionDao.getAll();
    }

    /**
     * Получить одно расширение по ID (синхронно).
     */
    public CachedExtensionEntity getCachedExtensionById(int id) {
        return cachedExtensionDao.getById(id);
    }

    /**
     * Обновить кеш расширений из сети (асинхронно в фоне).
     * После получения данных обновляет локальное хранилище (Room).
     *
     * @param page номер страницы
     * @param perPage количество элементов на странице
     */
    public void refreshExtensionsFromRemote(int page, int perPage) {
        executor.execute(() -> {
            try {
                List<ExtensionDto> remoteList = remote.fetchExtensions(page, perPage);
                if (remoteList != null && !remoteList.isEmpty()) {
                    // Преобразуем DTO в Entity через маппер
                    List<CachedExtensionEntity> entityList =
                            DtoToEntityMapper.mapExtensionListDtoToEntity(remoteList);

                    // Простая стратегия: очищаем кеш и вставляем новые данные
                    cachedExtensionDao.clearAll();
                    for (CachedExtensionEntity entity : entityList) {
                        cachedExtensionDao.insert(entity);
                    }
                }
            } catch (Exception ex) {
                // TODO: логирование/обработка ошибок через обработчик ошибок или callback
                ex.printStackTrace();
            }
        });
    }

    // ===== КАТЕГОРИИ =====

    /**
     * Получить список всех кешированных категорий (синхронно).
     */
    public List<CachedCategoryEntity> getCachedCategories() {
        return cachedCategoryDao.getAll();
    }

    /**
     * Обновить кеш категорий из сети (асинхронно).
     */
    public void refreshCategoriesFromRemote() {
        executor.execute(() -> {
            try {
                List<CategoryDto> remoteList = remote.fetchCategories();
                if (remoteList != null && !remoteList.isEmpty()) {
                    List<CachedCategoryEntity> entityList =
                            DtoToEntityMapper.mapCategoryListDtoToEntity(remoteList);
                    cachedCategoryDao.clearAll();
                    for (CachedCategoryEntity entity : entityList) {
                        cachedCategoryDao.insert(entity);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // ===== УСТАНОВЛЕННЫЕ РАСШИРЕНИЯ =====

    /**
     * Получить список установленных расширений (синхронно).
     *
     * @return список InstalledExtensionEntity с локальными зависимостями от данного устройства
     */
    public List<InstalledExtensionEntity> getInstalledExtensions() {
        return installedExtensionDao.getAll();
    }

    /**
     * Получить одно установленное расширение по ID.
     */
    public InstalledExtensionEntity getInstalledExtensionById(int id) {
        return installedExtensionDao.getById(id);
    }

    /**
     * Отметить расширение как установленное (локально).
     *
     * @param extensionId ID расширения из каталога
     * @param installedVersion версия, которая установлена
     * @return ID установленного расширения в таблице installed_extensions
     */
    public long installExtension(int extensionId, String installedVersion) {
        InstalledExtensionEntity entity = new InstalledExtensionEntity();
        entity.extensionId = extensionId;
        entity.installedVersion = installedVersion;
        entity.installedAt = System.currentTimeMillis();
        entity.isEnabled = true;
        entity.needsUpdate = false;
        // Grid-параметры — значения по умолчанию (потом пользователь может изменить)
        entity.gridRow = 0;
        entity.gridColumn = 0;
        entity.gridSpanWidth = 1;
        entity.gridSpanHeight = 1;
        entity.isHidden = false;

        return installedExtensionDao.insert(entity);
    }

    /**
     * Удалить установленное расширение.
     */
    public void uninstallExtension(int installedId) {
        InstalledExtensionEntity entity = installedExtensionDao.getById(installedId);
        if (entity != null) {
            // Удаляем JAR-файл и метаданные
            downloadManager.deleteExtension(installedId);
            // Удаляем запись из БД
            installedExtensionDao.delete(entity);
        }
    }

    /**
     * Обновить статус установленного расширения (включено/выключено, версия и т.д.).
     */
    public void updateInstalledExtension(InstalledExtensionEntity entity) {
        installedExtensionDao.update(entity);
    }

    // ===== ЗАГРУЗКА ФАЙЛОВ =====

    /**
     * Скачать JAR-файл расширения.
     *
     * @param installedId ID установленного расширения
     * @param extensionName название расширения
     * @param version версия
     * @param downloadUrl URL для скачивания (из сервера)
     * @param sha256 ожидаемый SHA256 хеш
     * @param fileSize размер файла в байтах
     * @return успешность загрузки
     */
    public boolean downloadExtensionJar(
            int installedId,
            String extensionName,
            String version,
            String downloadUrl,
            String sha256,
            long fileSize) {
        // TODO: это может быть долгая операция, стоит запустить в separate потоке
        return downloadManager.downloadExtension(
                installedId,
                extensionName,
                version,
                downloadUrl,
                sha256,
                fileSize
        );
    }

    /**
     * Получить локальный путь к JAR-файлу расширения.
     */
    public String getExtensionJarPath(int installedId) {
        return downloadManager.getExtensionJarPath(installedId);
    }

    /**
     * Проверить целостность JAR-файла расширения.
     */
    public boolean verifyExtensionFile(int installedId) {
        return downloadManager.verifyExtensionIntegrity(installedId);
    }
}


