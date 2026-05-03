# Room Persistence Library Guide

## Что такое Room?

**Room** — это абстракция над SQLite базой данных для Android. Она предоставляет слой компиляции и упрощает работу с локальным хранилищем данных, используя аннотации и типобезопасность на уровне компиляции.

## Основные компоненты Room

### 1. **Entity (Сущность)**
Класс, который представляет таблицу в базе данных. Каждое поле класса соответствует столбцу таблицы.

```java
@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String username;
    
    @ColumnInfo(name = "created_at")
    public long createdAt;
}
```

**Ключевые аннотации:**
- `@Entity` — помечает класс как таблицу БД
- `@PrimaryKey` — обозначает первичный ключ
- `@ColumnInfo` — задает имя столбца в таблице (если отличается от имени поля)
- `@Ignore` — исключить поле из БД

### 2. **DAO (Data Access Object)**
Интерфейс с методами для CRUD операций (Create, Read, Update, Delete) над Entity.

```java
@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);
    
    @Query("SELECT * FROM users WHERE id = :userId")
    UserEntity getUserById(int userId);
    
    @Update
    void update(UserEntity user);
    
    @Delete
    void delete(UserEntity user);
}
```

**Аннотации:**
- `@Insert` — вставить данные
- `@Update` — обновить данные
- `@Delete` — удалить данные
- `@Query` — выполнить SQL запрос

### 3. **Database (База данных)**
Абстрактный класс, который расширяет `RoomDatabase` и содержит все Entity и DAO'шки приложения.

```java
@Database(entities = {UserEntity.class, ExtensionEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ExtensionDao extensionDao();
    
    // Синглтон экземпляр БД
    private static AppDatabase instance;
    
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "multitool_db")
                    .build();
        }
        return instance;
    }
}
```

## Типичный поток работы

1. **Создаешь Entity** → класс, аннотированный `@Entity`
2. **Создаешь DAO** → методы для работы с Entity
3. **Определяешь Database** → объединяешь Entity и DAO
4. **Инициализируешь БД** → получаешь экземпляр через `Room.databaseBuilder()`
5. **Используешь в коде** → вызываешь методы DAO для работы с данными

## Связи между таблицами (Relationships)

### Один-к-одному (One-to-One)
Один пользователь имеет один профиль.

### Один-ко-многим (One-to-Many)
Один пользователь может иметь много установленных расширений.

### Много-ко-многим (Many-to-Many)
Одно расширение может принадлежать нескольким категориям. Требует промежуточную таблицу (CrossRef).

```java
// CrossRef таблица для связи Extension и Category
@Entity(
    primaryKeys = {"extensionId", "categoryId"},
    foreignKeys = {
        @ForeignKey(entity = ExtensionEntity.class, parentColumns = "id", childColumns = "extensionId"),
        @ForeignKey(entity = CategoryEntity.class, parentColumns = "id", childColumns = "categoryId")
    }
)
public class ExtensionCategoryCrossRef {
    public int extensionId;
    public int categoryId;
}
```

## Асинхронные операции

Room работает с Future/LiveData для асинхронных операций в UI потоке:

```java
@Query("SELECT * FROM extensions")
LiveData<List<ExtensionEntity>> getAllExtensions(); // автоматически обновляет UI
```

## Миграции версий БД

При изменении схемы БД нужно увеличить версию и описать миграцию:

```java
@Database(entities = {...}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQL для добавления новой колонки
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
        }
    };
}
```

## Best Practices

✅ Используй `@NonNull` и `@Nullable` для полей

✅ Используй `LiveData` или `Flow` для наблюдения изменений

✅ Создавай DAO отдельно для каждой Entity

✅ Инициализируй БД один раз (синглтон паттерн)

✅ Выполняй долгие операции в фоновом потоке

---

❌ Не выполняй DB операции в main потоке

❌ Не создавай несколько экземпляров БД

❌ Не забывай обновлять версию при изменении схемы

