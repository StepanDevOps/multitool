package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface CachedExtensionDao {

    @Query("SELECT * FROM cached_extensions ORDER BY updatedAt DESC")
    List<CachedExtensionEntity> getAll();

    @Query("SELECT * FROM cached_extensions WHERE id = :id LIMIT 1")
    CachedExtensionEntity getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachedExtensionEntity e);

    @Query("DELETE FROM cached_extensions")
    void clearAll();

    @Delete
    void delete(CachedExtensionEntity e);
}

