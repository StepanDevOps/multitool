package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface LocalExtensionMetaDao {

    @Query("SELECT * FROM local_extension_meta WHERE installedExtensionId = :installedId")
    LocalExtensionMetaEntity getByInstalledId(int installedId);

    @Insert
    long insert(LocalExtensionMetaEntity meta);

    @Update
    void update(LocalExtensionMetaEntity meta);

    @Delete
    void delete(LocalExtensionMetaEntity meta);
}

