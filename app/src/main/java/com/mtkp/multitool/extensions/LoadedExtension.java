package com.mtkp.multitool.extensions;

import java.io.File;

/**
 * Runtime-состояние загруженного расширения.
 */
public class LoadedExtension {
    public final String extensionId;
    public final String displayName;
    public final int requiredApiVersion;
    public final File sourceFile;
    public final ExtensionInterface instance;
    public final ClassLoader classLoader;

    public LoadedExtension(
            String extensionId,
            String displayName,
            int requiredApiVersion,
            File sourceFile,
            ExtensionInterface instance,
            ClassLoader classLoader
    ) {
        this.extensionId = extensionId;
        this.displayName = displayName;
        this.requiredApiVersion = requiredApiVersion;
        this.sourceFile = sourceFile;
        this.instance = instance;
        this.classLoader = classLoader;
    }
}

