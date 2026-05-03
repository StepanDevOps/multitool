package com.mtkp.multitool.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Сущность User — представляет таблицу пользователей в БД.
 * Каждая запись - это один зарегистрированный пользователь приложения.
 */
@Entity(tableName = "users")
public class UserEntity {
    
    /**
     * Уникальный идентификатор пользователя.
     * Auto-generate гарантирует, что каждый новый пользователь получит свой ID.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    /**
     * Имя пользователя (3-20 символов).
     * Не может быть null.
     */
    @NonNull
    public String username;
    
    /**
     * Email пользователя для входа и восстановления пароля.
     * Может быть пустым, если пользователь не создавал аккаунт.
     */
    @Nullable
    public String email;
    
    /**
     * Путь к аватарке пользователя на файловой системе.
     * Пример: "/data/data/com.mtkp.multitool/avatars/user_1.png"
     * Nullable, так как по умолчанию может быть стандартная аватарка.
     */
    @Nullable
    public String avatarPath;
    
    /**
     * Дата создания аккаунта (время в миллисекундах с 1970 года).
     * System.currentTimeMillis() при создании пользователя.
     */
    public long createdAt;
}
