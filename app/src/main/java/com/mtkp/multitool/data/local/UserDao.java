package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

/**
 * DAO для работы с пользователями (UserEntity).
 */
@Dao
public interface UserDao {

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getById(int id);

    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);
}

