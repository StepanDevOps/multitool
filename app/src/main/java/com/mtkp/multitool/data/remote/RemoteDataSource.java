package com.mtkp.multitool.data.remote;

import android.content.Context;

import com.mtkp.multitool.data.remote.dto.AuthDto;
import com.mtkp.multitool.data.remote.dto.AuthLoginRequestDto;
import com.mtkp.multitool.data.remote.dto.AuthRegisterRequestDto;
import com.mtkp.multitool.data.remote.dto.AuthVerifyResponseDto;
import com.mtkp.multitool.data.remote.dto.CategoryDto;
import com.mtkp.multitool.data.remote.dto.CategoriesResponseDto;
import com.mtkp.multitool.data.remote.dto.CreateRatingRequestDto;
import com.mtkp.multitool.data.remote.dto.CreateExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.CreateInstalledExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.ExtensionMetaDto;
import com.mtkp.multitool.data.remote.dto.ExtensionsListResponseDto;
import com.mtkp.multitool.data.remote.dto.InstalledExtensionDto;
import com.mtkp.multitool.data.remote.dto.InstalledExtensionsResponseDto;
import com.mtkp.multitool.data.remote.dto.RatingDto;
import com.mtkp.multitool.data.remote.dto.RatingsResponseDto;
import com.mtkp.multitool.data.remote.dto.UpdateExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.UpdateRatingRequestDto;
import com.mtkp.multitool.data.remote.dto.UpdateInstalledExtensionRequestDto;
import com.mtkp.multitool.data.remote.dto.UploadVersionResponseDto;
import com.mtkp.multitool.data.remote.network.ApiClient;
import com.mtkp.multitool.data.remote.network.BackendApiService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Реализация удалённого источника данных через Retrofit.
 * Внешний код работает с абстракцией ExtensionsApi и не зависит от Retrofit напрямую.
 */
public class RemoteDataSource implements ExtensionsApi {

    private final BackendApiService api;
    private final Map<String, String> lastDownloadSha = new ConcurrentHashMap<>();

    public RemoteDataSource(Context context) {
        this.api = new ApiClient(context).service();
    }

    @Override
    public AuthDto register(String username, String email, String password) throws Exception {
        AuthRegisterRequestDto body = new AuthRegisterRequestDto(username, email, password);
        return bodyOrThrow(api.register(body).execute(), "register");
    }

    @Override
    public AuthDto login(String email, String password) throws Exception {
        AuthLoginRequestDto body = new AuthLoginRequestDto(email, password);
        return bodyOrThrow(api.login(body).execute(), "login");
    }

    @Override
    public AuthVerifyResponseDto verifyToken() throws Exception {
        return bodyOrThrow(api.verifyToken().execute(), "verify token");
    }

    @Override
    public List<ExtensionDto> fetchExtensions(
            int page,
            int perPage,
            String category,
            String sort,
            String search
    ) throws Exception {
        ExtensionsListResponseDto response = bodyOrThrow(
                api.getExtensions(page, perPage, category, sort, search).execute(),
                "get extensions"
        );
        return response.data;
    }

    @Override
    public ExtensionDto fetchExtensionById(int id) throws Exception {
        return bodyOrThrow(api.getExtensionById(id).execute(), "get extension by id");
    }

    @Override
    public ExtensionMetaDto fetchExtensionMeta(int id) throws Exception {
        return bodyOrThrow(api.getExtensionMeta(id).execute(), "get extension meta");
    }

    @Override
    public List<CategoryDto> fetchCategories() throws Exception {
        CategoriesResponseDto response = bodyOrThrow(api.getCategories().execute(), "get categories");
        return response.data;
    }

    @Override
    public List<InstalledExtensionDto> fetchInstalledExtensions(int userId) throws Exception {
        InstalledExtensionsResponseDto response = bodyOrThrow(
                api.getInstalledExtensions(userId).execute(),
                "get installed extensions"
        );
        return response.data;
    }

    @Override
    public InstalledExtensionDto createInstalledExtension(
            int userId,
            int extensionId,
            String installedVersion
    ) throws Exception {
        CreateInstalledExtensionRequestDto body =
                new CreateInstalledExtensionRequestDto(extensionId, installedVersion);
        return bodyOrThrow(
                api.createInstalledExtension(userId, body).execute(),
                "create installed extension"
        );
    }

    @Override
    public InstalledExtensionDto updateInstalledExtension(
            int userId,
            int installedId,
            Boolean isEnabled,
            String installedVersion
    ) throws Exception {
        UpdateInstalledExtensionRequestDto body =
                new UpdateInstalledExtensionRequestDto(isEnabled, installedVersion);
        return bodyOrThrow(
                api.updateInstalledExtension(userId, installedId, body).execute(),
                "update installed extension"
        );
    }

    @Override
    public void deleteInstalledExtension(int userId, int installedId) throws Exception {
        Response<Void> response = api.deleteInstalledExtension(userId, installedId).execute();
        if (!response.isSuccessful()) {
            throw new IllegalStateException("delete installed extension failed: HTTP " + response.code());
        }
    }

    @Override
    public ExtensionDto createExtension(
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories
    ) throws Exception {
        CreateExtensionRequestDto body =
                new CreateExtensionRequestDto(name, shortDescription, detailedDescription, categories);
        return bodyOrThrow(api.createExtension(body).execute(), "create extension");
    }

    @Override
    public ExtensionDto updateExtension(
            int extensionId,
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories
    ) throws Exception {
        UpdateExtensionRequestDto body =
                new UpdateExtensionRequestDto(name, shortDescription, detailedDescription, categories);
        return bodyOrThrow(api.updateExtension(extensionId, body).execute(), "update extension");
    }

    @Override
    public UploadVersionResponseDto uploadVersion(
            int extensionId,
            String version,
            String releaseNotes,
            File jarFile,
            String changelog
    ) throws Exception {
        RequestBody versionPart = RequestBody.create(version, MediaType.parse("text/plain"));
        RequestBody notesPart = RequestBody.create(releaseNotes, MediaType.parse("text/plain"));
        RequestBody changelogPart = RequestBody.create(
                changelog == null ? "" : changelog,
                MediaType.parse("text/plain")
        );

        RequestBody fileBody = RequestBody.create(
                jarFile,
                MediaType.parse("application/java-archive")
        );
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "jarFile",
                jarFile.getName(),
                fileBody
        );

        return bodyOrThrow(
                api.uploadVersion(extensionId, versionPart, notesPart, filePart, changelogPart).execute(),
                "upload extension version"
        );
    }

    @Override
    public ResponseBody downloadExtension(int extensionId, String version) throws Exception {
        retrofit2.Response<ResponseBody> response = api.downloadExtension(extensionId, version).execute();
        if (response.isSuccessful() && response.body() != null) {
            // Сохраняем SHA, если сервер вернул заголовок X-File-SHA256
            String sha = null;
            try {
                sha = response.headers().get("X-File-SHA256");
            } catch (Exception ignored) {
            }
            if (sha == null) {
                try {
                    sha = response.headers().get("x-file-sha256");
                } catch (Exception ignored) {
                }
            }
            if (sha != null && !sha.isEmpty()) {
                lastDownloadSha.put(extensionId + ":" + (version == null ? "" : version), sha);
            }
            return response.body();
        }
        throw new ApiRequestException(response.code(), "download extension failed: HTTP " + response.code());
    }

    /**
     * Получить SHA из последнего скачивания для пары extensionId:version если сервер вернул заголовок.
     */
    public String getLastDownloadedSha(int extensionId, String version) {
        return lastDownloadSha.get(extensionId + ":" + (version == null ? "" : version));
    }

    @Override
    public List<RatingDto> fetchRatings(int extensionId, int page, int perPage) throws Exception {
        RatingsResponseDto response = bodyOrThrow(
                api.getRatings(extensionId, page, perPage).execute(),
                "get ratings"
        );
        return response.data;
    }

    @Override
    public RatingDto createRating(int extensionId, int rating, String review) throws Exception {
        CreateRatingRequestDto body = new CreateRatingRequestDto(rating, review);
        return bodyOrThrow(api.createRating(extensionId, body).execute(), "create rating");
    }

    @Override
    public RatingDto updateRating(int extensionId, int ratingId, Integer rating, String review) throws Exception {
        UpdateRatingRequestDto body = new UpdateRatingRequestDto(rating, review);
        return bodyOrThrow(api.updateRating(extensionId, ratingId, body).execute(), "update rating");
    }

    @Override
    public void deleteRating(int extensionId, int ratingId) throws Exception {
        Response<Void> response = api.deleteRating(extensionId, ratingId).execute();
        if (!response.isSuccessful()) {
            throw new ApiRequestException(
                    response.code(),
                    "delete rating failed: HTTP " + response.code()
            );
        }
    }

    private <T> T bodyOrThrow(Response<T> response, String operation) {
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        throw new ApiRequestException(
                response.code(),
                operation + " failed: HTTP " + response.code() +
                        (response.message().isEmpty() ? "" : " " + response.message())
        );
    }
}

