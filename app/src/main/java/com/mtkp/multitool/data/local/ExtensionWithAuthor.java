package com.mtkp.multitool.data.local;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * POJO для удобного получения расширения вместе с данными автора (UserEntity).
 * Позволяет использовать @Relation, чтобы Room автоматически подгружал пользователя.
 */
public class ExtensionWithAuthor {

    @Embedded
    public ExtensionEntity extension;

    @Relation(parentColumn = "authorId", entityColumn = "id")
    public UserEntity author;
}

