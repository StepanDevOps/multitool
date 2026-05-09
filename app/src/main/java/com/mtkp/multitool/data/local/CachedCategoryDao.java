package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

/**
 * DAO для кеша категорий.
 */
@Dao
public interface CachedCategoryDao {

    @Query("SELECT * FROM cached_categories ORDER BY name")
    List<CachedCategoryEntity> getAll();

    @Query("SELECT * FROM cached_categories WHERE id = :id LIMIT 1")
    CachedCategoryEntity getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachedCategoryEntity e);

    @Query("DELETE FROM cached_categories")
    void clearAll();

    @Delete
    void delete(CachedCategoryEntity e);
}

