package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Описание таблицы "Заметки"
@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String content;
}
