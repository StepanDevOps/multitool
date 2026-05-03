package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAll();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    CategoryEntity getById(int id);

    @Insert
    long insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);
}

