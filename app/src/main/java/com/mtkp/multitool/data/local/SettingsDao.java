package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

/**
 * DAO для работы с настройками (SettingsEntity).
 * Используем Key-Value паттерн; для вставки/обновления применяем REPLACE стратегию.
 */
@Dao
public interface SettingsDao {

    @Query("SELECT * FROM settings")
    List<SettingsEntity> getAll();

    @Query("SELECT * FROM settings WHERE `key` = :key ORDER BY lastUpdated DESC LIMIT 1")
    SettingsEntity getByKey(String key);

    @Query("DELETE FROM settings WHERE `key` = :key")
    void deleteByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrReplace(SettingsEntity setting);

    @Update
    void update(SettingsEntity setting);

    @Delete
    void delete(SettingsEntity setting);
}

