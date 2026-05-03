package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ExtensionVersionDao {

    @Query("SELECT * FROM extension_versions WHERE extensionId = :extensionId ORDER BY releasedAt DESC")
    List<ExtensionVersionEntity> getByExtensionId(int extensionId);

    @Query("SELECT * FROM extension_versions WHERE id = :id LIMIT 1")
    ExtensionVersionEntity getById(int id);

    @Insert
    long insert(ExtensionVersionEntity version);

    @Update
    void update(ExtensionVersionEntity version);

    @Delete
    void delete(ExtensionVersionEntity version);
}

