package com.mtkp.multitool.data.repository;

import android.content.Context;

import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.CachedExtensionDao;
import com.mtkp.multitool.data.local.CachedExtensionEntity;
import com.mtkp.multitool.data.remote.ExtensionsApi;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Простой репозиторий для работы с каталогом расширений.
 *
 * Поведение (упрощённое):
 * - getCachedExtensions() — возвращает локальный кеш (Room)
 * - refreshFromRemote() — загружает список с сервера (через RemoteDataSource) и обновляет кеш
 */
public class ExtensionsRepository {

    private final CachedExtensionDao cachedDao;
    private final ExtensionsApi remote;
    private final ExecutorService executor;

    public ExtensionsRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.cachedDao = db.cachedExtensionDao();
        this.remote = new RemoteDataSource();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Вернуть локальный кеш (синхронно). UI должен вызывать это в фоновом потоке или через презентер.
     */
    public List<CachedExtensionEntity> getCachedExtensions() {
        if (cachedDao == null) return new ArrayList<>();
        return cachedDao.getAll();
    }

    /**
     * Обновить кеш из сети (в фоне). Колбеков пока нет — можно будет расширить.
     */
    public void refreshFromRemote(int page, int perPage) {
        executor.execute(() -> {
            try {
                List<ExtensionDto> remoteList = remote.fetchExtensions(page, perPage);
                if (cachedDao != null) {
                    // простая стратегия: очистить кеш и записать заново
                    cachedDao.clearAll();
                    for (ExtensionDto d : remoteList) {
                        CachedExtensionEntity e = new CachedExtensionEntity();
                        e.id = d.id;
                        e.name = d.name != null ? d.name : "";
                        e.shortDescription = d.shortDescription;
                        e.logoUrl = d.logoUrl;
                        e.categoriesCsv = d.categories == null ? "" : String.join(",", d.categories);
                        e.version = d.version;
                        e.downloads = d.downloads;
                        e.rating = d.rating;
                        e.badge = d.badge;
                        e.updatedAt = d.updatedAt;
                        cachedDao.insert(e);
                    }
                }
            } catch (Exception ex) {
                // TODO: логирование/обработка ошибок
                ex.printStackTrace();
            }
        });
    }
}


