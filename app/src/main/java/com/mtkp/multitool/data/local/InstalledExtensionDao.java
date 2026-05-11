package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

/**
 * DAO для работы с установленными расширениями на устройстве.
 */
@Dao
public interface InstalledExtensionDao {

    @Query("SELECT * FROM installed_extensions")
    List<InstalledExtensionEntity> getAll();

    @Query("SELECT * FROM installed_extensions WHERE id = :id LIMIT 1")
    InstalledExtensionEntity getById(int id);

    @Query("SELECT * FROM installed_extensions WHERE extensionId = :extensionId LIMIT 1")
    InstalledExtensionEntity getByExtensionId(int extensionId);


    @Insert
    long insert(InstalledExtensionEntity installed);

    @Update
    void update(InstalledExtensionEntity installed);

    @Delete
    void delete(InstalledExtensionEntity installed);
}

