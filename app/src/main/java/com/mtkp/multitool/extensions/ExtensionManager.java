package com.mtkp.multitool.extensions;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.mtkp.multitool.data.remote.ExtensionsApi;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.ExtensionMetaDto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dalvik.system.DexClassLoader;
import okhttp3.ResponseBody;

/**
 * ExtensionManager — простой менеджер расширений: загрузка, установка и запуск.
 *
 * Сейчас реализован минимальный skeleton: фоновая загрузка файла и сохранение
 * в приватную директорию приложения + runtime-loading (DexClassLoader).
 */
public class ExtensionManager {

    private static final String TAG = "ExtensionManager";
    public static final String DEFAULT_ENTRY_CLASS = "com.mtkp.multitool.plugin.PluginEntry";

    private final Context appContext;
    private final ExtensionsApi api;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final ExtensionHostApi hostApi;
    private final Map<String, LoadedExtension> loadedExtensions = new HashMap<>();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable t);
    }

    public ExtensionManager(Context context) {
        this(context, new RemoteDataSource(context));
    }

    public ExtensionManager(Context context, ExtensionsApi api) {
        this.appContext = context.getApplicationContext();
        this.api = api;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.hostApi = new SettingsBackedExtensionHostApi(appContext);
    }

    /** Получить список доступных расширений (фон) */
    public void listAvailable(int page, int perPage, Callback<List<ExtensionDto>> callback) {
        executor.execute(() -> {
            try {
                List<ExtensionDto> list = api.fetchExtensions(page, perPage);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                Log.e(TAG, "listAvailable failed", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Скачать и сохранить расширение локально. Файл сохраняется в <files>/extensions/.
     * Верификация/установка/загрузка в рантайме — TODO.
     */
    public void downloadAndSave(int extensionId, String version, Callback<File> callback) {
        executor.execute(() -> {
            try {
                String targetVersion = version;
                String expectedSha256 = null;
                ExtensionMetaDto meta = api.fetchExtensionMeta(extensionId);
                if (meta != null) {
                    if (TextUtils.isEmpty(targetVersion)) {
                        targetVersion = meta.currentVersion;
                    }
                    expectedSha256 = meta.sha256;
                }
                if (TextUtils.isEmpty(targetVersion)) {
                    throw new IllegalStateException("Extension version is missing");
                }

                File out;
                try (ResponseBody body = api.downloadExtension(extensionId, targetVersion)) {
                if (body == null) throw new IllegalStateException("Empty response body");

                File dir = new File(appContext.getFilesDir(), "extensions");
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        Log.w(TAG, "Failed to create extensions directory: " + dir.getAbsolutePath());
                    }
                }

                out = new File(dir, extensionId + "-" + targetVersion + ".jar");
                try (InputStream is = body.byteStream(); FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buffer = new byte[8 * 1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                }
                }

                // Если сервер отдал контрольную сумму — валидируем до активации.
                // Если expectedSha256 не задана в метаданных, попробуем получить её из заголовка скачивания (RemoteDataSource).
                if (TextUtils.isEmpty(expectedSha256) && api instanceof com.mtkp.multitool.data.remote.RemoteDataSource) {
                    try {
                        expectedSha256 = ((com.mtkp.multitool.data.remote.RemoteDataSource) api)
                                .getLastDownloadedSha(extensionId, targetVersion);
                    } catch (Exception ignored) {
                    }
                }

                if (!TextUtils.isEmpty(expectedSha256)) {
                    String localHash = computeSha256(out);
                    if (!expectedSha256.equalsIgnoreCase(localHash)) {
                        // Удаляем поврежденный файл, чтобы не оставить «битую» установку.
                        //noinspection ResultOfMethodCallIgnored
                        out.delete();
                        throw new IllegalStateException("SHA-256 mismatch for extension " + extensionId);
                    }
                }

                mainHandler.post(() -> callback.onSuccess(out));
            } catch (Exception e) {
                Log.e(TAG, "downloadAndSave failed", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Загрузить установленное расширение из jar/apk, создать инстанс и вызвать lifecycle.
     */
    public void loadInstalled(File file, String entryClassName, Callback<LoadedExtension> callback) {
        executor.execute(() -> {
            try {
                LoadedExtension loaded = loadFromFileInternal(file, entryClassName);
                mainHandler.post(() -> callback.onSuccess(loaded));
            } catch (Exception e) {
                Log.e(TAG, "loadInstalled failed", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Совместимость со старым вызовом.
     */
    public void loadInstalled(File file, Callback<Boolean> callback) {
        loadInstalled(file, DEFAULT_ENTRY_CLASS, new Callback<LoadedExtension>() {
            @Override
            public void onSuccess(LoadedExtension result) {
                callback.onSuccess(true);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }
        });
    }

    /**
     * Полный сценарий: скачать расширение и сразу активировать.
     */
    public void downloadAndLoad(int extensionId,
                                String version,
                                String entryClassName,
                                Callback<LoadedExtension> callback) {
        downloadAndSave(extensionId, version, new Callback<File>() {
            @Override
            public void onSuccess(File file) {
                loadInstalled(file, entryClassName, callback);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void unload(String extensionId, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                LoadedExtension loaded = loadedExtensions.remove(extensionId);
                if (loaded != null) {
                    loaded.instance.onUnload();
                }
                mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                Log.e(TAG, "unload failed", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /** Удалить локально сохранённое расширение */
    public void uninstall(File file, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                // Если extension была активна и удаляют её бинарник, сначала выгружаем.
                for (Map.Entry<String, LoadedExtension> entry : new HashMap<>(loadedExtensions).entrySet()) {
                    LoadedExtension loaded = entry.getValue();
                    if (loaded != null && loaded.sourceFile != null && loaded.sourceFile.equals(file)) {
                        loaded.instance.onUnload();
                        loadedExtensions.remove(entry.getKey());
                    }
                }
                boolean deleted = file != null && file.exists() && file.delete();
                mainHandler.post(() -> callback.onSuccess(deleted));
            } catch (Exception e) {
                Log.e(TAG, "uninstall failed", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public LoadedExtension getLoadedById(String extensionId) {
        return loadedExtensions.get(extensionId);
    }

    private LoadedExtension loadFromFileInternal(File file, String entryClassName) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("Extension file not found");
        }
        String className = TextUtils.isEmpty(entryClassName) ? DEFAULT_ENTRY_CLASS : entryClassName;

        File dexOptDir = new File(appContext.getCodeCacheDir(), "ext_dex");
        if (!dexOptDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dexOptDir.mkdirs();
        }

        DexClassLoader classLoader = new DexClassLoader(
                file.getAbsolutePath(),
                dexOptDir.getAbsolutePath(),
                null,
                appContext.getClassLoader()
        );

        Class<?> rawClass = classLoader.loadClass(className);
        if (!ExtensionInterface.class.isAssignableFrom(rawClass)) {
            throw new IllegalStateException("Entry class does not implement ExtensionInterface: " + className);
        }

        ExtensionInterface instance = (ExtensionInterface) rawClass.getDeclaredConstructor().newInstance();
        int requiredApi = instance.getRequiredApiVersion();
        if (requiredApi > ExtensionInterface.HOST_API_VERSION) {
            throw new IllegalStateException(
                    String.format(
                            Locale.US,
                            "Unsupported extension API. required=%d, host=%d",
                            requiredApi,
                            ExtensionInterface.HOST_API_VERSION
                    )
            );
        }

        instance.onLoad(appContext, hostApi);

        LoadedExtension loaded = new LoadedExtension(
                instance.getExtensionId(),
                instance.getDisplayName(),
                requiredApi,
                file,
                instance,
                classLoader
        );
        loadedExtensions.put(loaded.extensionId, loaded);
        return loaded;
    }

    private String computeSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }
}


