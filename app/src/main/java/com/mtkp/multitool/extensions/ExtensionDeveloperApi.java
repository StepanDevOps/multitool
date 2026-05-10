package com.mtkp.multitool.extensions;

import android.content.Context;

import com.mtkp.multitool.data.remote.ExtensionsApi;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
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
}

