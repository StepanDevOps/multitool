package com.mtkp.multitool.features.extensions;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Модель одного расширения.
 * <p>
 * В ней храним только те данные, которые нужны для визуального
 * показа в магазине и на экране расширения.
 */
public class ExtensionItem {

    private final String id;
    private final String title;
    private final String author;
    private final String version;
    private final int installs;
    private final float rating;
    private final @StringRes int categoryResId;
    private final String shortDescription;
    private final String markdownDescription;
    private final @DrawableRes int iconResId;
    private final boolean installed;
    private final boolean updateAvailable;

    public ExtensionItem(String id,
                         String title,
                         String author,
                         String version,
                         int installs,
                         float rating,
                         @StringRes int categoryResId,
                         String shortDescription,
                         String markdownDescription,
                         @DrawableRes int iconResId,
                         boolean installed,
                         boolean updateAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.version = version;
        this.installs = installs;
        this.rating = rating;
        this.categoryResId = categoryResId;
        this.shortDescription = shortDescription;
        this.markdownDescription = markdownDescription;
        this.iconResId = iconResId;
        this.installed = installed;
        this.updateAvailable = updateAvailable;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public int getInstalls() {
        return installs;
    }

    public float getRating() {
        return rating;
    }

    public int getCategoryResId() {
        return categoryResId;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getMarkdownDescription() {
        return markdownDescription;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}

