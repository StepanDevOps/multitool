package com.mtkp.multitool.extensions;

import android.content.Context;

import com.mtkp.multitool.data.remote.ExtensionsApi;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.RatingDto;
import com.mtkp.multitool.data.remote.dto.UploadVersionResponseDto;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * API для разработчиков расширений внутри приложения:
 * создание карточки расширения и загрузка версии бинарника.
 */
public class ExtensionDeveloperApi {

    private final ExtensionsApi api;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface Callback<T> {
        void onSuccess(T data);

        void onError(Throwable throwable);
    }

    public ExtensionDeveloperApi(Context context) {
        this(new RemoteDataSource(context));
    }

    public ExtensionDeveloperApi(ExtensionsApi api) {
        this.api = api;
    }

    public void createExtension(
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories,
            Callback<ExtensionDto> callback
    ) {
        executor.execute(() -> {
            try {
                ExtensionDto dto = api.createExtension(name, shortDescription, detailedDescription, categories);
                callback.onSuccess(dto);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void uploadVersion(
            int extensionId,
            String version,
            String releaseNotes,
            File jarFile,
            String changelog,
            Callback<UploadVersionResponseDto> callback
    ) {
        executor.execute(() -> {
            try {
                UploadVersionResponseDto dto = api.uploadVersion(
                        extensionId,
                        version,
                        releaseNotes,
                        jarFile,
                        changelog
                );
                callback.onSuccess(dto);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateExtensionMetadata(
            int extensionId,
            String name,
            String shortDescription,
            String detailedDescription,
            List<String> categories,
            Callback<ExtensionDto> callback
    ) {
        executor.execute(() -> {
            try {
                ExtensionDto dto = api.updateExtension(
                        extensionId,
                        name,
                        shortDescription,
                        detailedDescription,
                        categories
                );
                callback.onSuccess(dto);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void submitReview(int extensionId, int rating, String review, Callback<RatingDto> callback) {
        executor.execute(() -> {
            try {
                RatingDto dto = api.createRating(extensionId, rating, review);
                callback.onSuccess(dto);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getReviews(int extensionId, int page, int perPage, Callback<List<RatingDto>> callback) {
        executor.execute(() -> {
            try {
                List<RatingDto> list = api.fetchRatings(extensionId, page, perPage);
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateReview(int extensionId, int ratingId, Integer rating, String review, Callback<RatingDto> callback) {
        executor.execute(() -> {
            try {
                RatingDto dto = api.updateRating(extensionId, ratingId, rating, review);
                callback.onSuccess(dto);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteReview(int extensionId, int ratingId, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                api.deleteRating(extensionId, ratingId);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}
