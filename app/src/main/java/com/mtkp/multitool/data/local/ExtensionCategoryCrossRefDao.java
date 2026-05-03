package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ExtensionCategoryCrossRefDao {

    @Query("SELECT * FROM extension_category_cross_ref")
    List<ExtensionCategoryCrossRef> getAll();

    @Insert
    void insert(ExtensionCategoryCrossRef ref);

    @Delete
    void delete(ExtensionCategoryCrossRef ref);

    @Query("SELECT categoryId FROM extension_category_cross_ref WHERE extensionId = :extensionId")
    List<Integer> getCategoryIdsForExtension(int extensionId);
}

