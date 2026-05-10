package com.mtkp.multitool.data.remote;

import com.mtkp.multitool.data.remote.dto.AuthDto;
import com.mtkp.multitool.data.remote.dto.AuthVerifyResponseDto;
import com.mtkp.multitool.data.remote.dto.CategoryDto;
import com.mtkp.multitool.data.remote.dto.ExtensionMetaDto;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.InstalledExtensionDto;
import com.mtkp.multitool.data.remote.dto.RatingDto;
import com.mtkp.multitool.data.remote.dto.UploadVersionResponseDto;

import java.io.File;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Простой интерфейс API для получения данных о расширениях.
 *
 * Примечание: это не привязано к конкретной реализации сети — позже можно
 * подключить Retrofit/OkHttp и реализовать методы в RemoteDataSource.
 */
public interface ExtensionsApi {

    AuthDto register(String username, String email, String password) throws Exception;

    AuthDto login(String email, String password) throws Exception;

    AuthVerifyResponseDto verifyToken() throws Exception;

    /**
     * Получить список расширений (страница/пагинация при необходимости).
     * Реализация должна быть асинхронной (в репозитории мы её вызовем в фоне).
     */
    default List<ExtensionDto> fetchExtensions(int page, int perPage) throws Exception {
        return fetchExtensions(page, perPage, null, "updated_at", null);
    }

    List<ExtensionDto> fetchExtensions(
            int page,
            int perPage,
            String category,
            String sort,
            String search
    ) throws Exception;

    /**
     * Получить одну запись расширения по id.
     */
    ExtensionDto fetchExtensionById(int id) throws Exception;

    ExtensionMetaDto fetchExtensionMeta(int id) throws Exception;

    /**
     * Получить список категорий расширений.
     */
    List<CategoryDto> fetchCategories() throws Exception;

    List<InstalledExtensionDto> fetchInstalledExtensions(int userId) throws Exception;

    InstalledExtensionDto createInstalledExtension(
            int userId,
            int extensionId,
            String installedVersion
    ) throws Exception;

    InstalledExtensionDto updateInstalledExtension(
            int userId,
            int installedId,
            Boolean isEnabled,
            String installedVersion
    ) throws Exception;

    void deleteInstalledExtension(int userId, int installedId) throws Exception;

    ExtensionDto createExtension(
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories
    ) throws Exception;

    ExtensionDto updateExtension(
            int extensionId,
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories
    ) throws Exception;

    UploadVersionResponseDto uploadVersion(
            int extensionId,
            String version,
            String releaseNotes,
            File jarFile,
            String changelog
    ) throws Exception;

    ResponseBody downloadExtension(int extensionId, String version) throws Exception;

    List<RatingDto> fetchRatings(int extensionId, int page, int perPage) throws Exception;

    RatingDto createRating(int extensionId, int rating, String review) throws Exception;

    RatingDto updateRating(int extensionId, int ratingId, Integer rating, String review) throws Exception;

    void deleteRating(int extensionId, int ratingId) throws Exception;
}

