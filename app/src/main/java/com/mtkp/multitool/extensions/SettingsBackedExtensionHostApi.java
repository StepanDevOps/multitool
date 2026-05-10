package com.mtkp.multitool.extensions;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mtkp.multitool.data.settings.SettingsStorage;

/**
 * Простая реализация host API для расширений через отдельный SharedPreferences namespace.
 */
public class SettingsBackedExtensionHostApi implements ExtensionHostApi {

    private static final String PREFS_NAME = "extensions_runtime";

    private final SharedPreferences prefs;
    private final SettingsStorage settingsStorage;
    private final PluginDataDbHelper pluginDataDbHelper;

    public SettingsBackedExtensionHostApi(Context context) {
        Context app = context.getApplicationContext();
        this.prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.settingsStorage = new SettingsStorage(app);
        this.pluginDataDbHelper = new PluginDataDbHelper(app);
    }

    @Override
    public void log(String tag, String message) {
        Log.d(tag == null ? "Extension" : tag, message == null ? "" : message);
    }

    @Override
    public String getSetting(String key, String defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        return prefs.getString(key, defaultValue);
    }

    @Override
    public void putSetting(String key, String value) {
        if (key == null) {
            return;
        }
        prefs.edit().putString(key, value).apply();
    }

    @Override
    public void removeSetting(String key) {
        if (key == null) {
            return;
        }
        prefs.edit().remove(key).apply();
    }

    @Override
    public String getCurrentUserName() {
        return settingsStorage.getUserName();
    }

    @Override
    public String getCurrentUserEmail() {
        return settingsStorage.getEmail();
    }

    @Override
    public void putPluginData(String extensionId, String key, String value) {
        if (extensionId == null || key == null) {
            return;
        }
        SQLiteDatabase db = pluginDataDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("extension_id", extensionId);
        cv.put("data_key", key);
        cv.put("data_value", value);
        db.insertWithOnConflict("plugin_data", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public String getPluginData(String extensionId, String key, String defaultValue) {
        if (extensionId == null || key == null) {
            return defaultValue;
        }
        SQLiteDatabase db = pluginDataDbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT data_value FROM plugin_data WHERE extension_id = ? AND data_key = ? LIMIT 1",
                new String[]{extensionId, key}
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return defaultValue;
        }
    }

    @Override
    public void removePluginData(String extensionId, String key) {
        if (extensionId == null || key == null) {
            return;
        }
        SQLiteDatabase db = pluginDataDbHelper.getWritableDatabase();
        db.execSQL(
                "DELETE FROM plugin_data WHERE extension_id = ? AND data_key = ?",
                new Object[]{extensionId, key}
        );
    }

    private static class PluginDataDbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "plugin_data.db";
        private static final int DB_VERSION = 1;

        PluginDataDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS plugin_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "extension_id TEXT NOT NULL," +
                            "data_key TEXT NOT NULL," +
                            "data_value TEXT," +
                            "UNIQUE(extension_id, data_key)" +
                            ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // v1: без миграций
        }
    }
}

