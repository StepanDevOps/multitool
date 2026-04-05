package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Сущность Room для таблицы заметок.
 * Каждый объект этого класса — одна строка в базе.
 */
@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String content;
}
