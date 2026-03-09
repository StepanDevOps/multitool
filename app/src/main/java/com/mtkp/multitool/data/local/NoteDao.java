package com.mtkp.multitool.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

// Команды: сохранить/удалить
@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes")
    List<NoteEntity> getAll();

    @Insert
    void insert(NoteEntity note);
}
