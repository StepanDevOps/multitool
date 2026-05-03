package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Transaction;

import java.util.List;

/**
 * DAO для работы с расширениями (ExtensionEntity).
 */
@Dao
public interface ExtensionDao {

    @Query("SELECT * FROM extensions")
    List<ExtensionEntity> getAll();

    @Query("SELECT * FROM extensions WHERE id = :id LIMIT 1")
    ExtensionEntity getById(int id);

    @Insert
    long insert(ExtensionEntity extension);

    @Update
    void update(ExtensionEntity extension);

    @Delete
    void delete(ExtensionEntity extension);

    // Получить расширения вместе с данными автора (UserEntity) через Relation
    @Transaction
    @Query("SELECT * FROM extensions")
    List<ExtensionWithAuthor> getAllWithAuthor();

    @Transaction
    @Query("SELECT * FROM extensions WHERE id = :id LIMIT 1")
    ExtensionWithAuthor getWithAuthorById(int id);
}

