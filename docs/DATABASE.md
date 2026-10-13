# База данных (Room)

Локальное хранилище реализовано на **Room 2.6.1**.
Класс-холдер: `db/CocktailsDatabase.java`.

```java
@Database(entities = {Drink.class, Ingredient.class, DrinkIngredientJoin.class},
          version = 1, exportSchema = false)
public abstract class CocktailsDatabase extends RoomDatabase
```

- Имя файла БД: `cocktails_db`
- Синглтон: `CocktailsDatabase.getInstance()`
- DAO: `getDrinkDao()`, `getIngredientDao()`, `getCocktailDao()`
- Флаг инициализации: `MutableLiveData<Boolean> getDatabaseCreated()`

## Схема

```
Drink 1 ────< DrinkIngredientJoin >──── 1 Ingredient
        (drink_id, FK)     (ingredient_id, FK)
```

`DrinkIngredientJoin` — таблица связи «многие-ко-многим» между напитками и ингредиентами.

## Сущности (`db/entity`)

### `Drink` — `@Entity`

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | `Long` | `@PrimaryKey(autoGenerate = true)` |
| `name` | `String` | Название |
| `description` | `String` | Описание |
| `tastes` | `ArrayList<Taste>` | Вкусы (JSON через `TasteConverter`) |
| `image` | `String` | Путь/URL к изображению |
| `glass` | `@Embedded Glass` | Бокал (встроенный объект) |
| `rating` | `int` | Оценка |
| `isAlcoholic` | `boolean` | Алкогольный |
| `isCarbonated` | `boolean` | Газированный |
| `notes` | `String` | Заметки |

Индекс: уникальный по `id`. Аннотация `@TypeConverters(TasteConverter.class)`.
Реализует `Serializable` (передаётся между экранами через Bundle).

### `Ingredient` — `@Entity`

| Поле | Тип |
|------|-----|
| `id` | `Long` (`@PrimaryKey(autoGenerate = true)`) |
| `name` | `String` |
| `description` | `String` |
| `image` | `String` |
| `notes` | `String` |

Уникальный индекс по `id`. Переопределены `equals`/`hashCode`/`toString`.

### `DrinkIngredientJoin` — `@Entity` (связь)

| Поле | Тип | Примечание |
|------|-----|-----------|
| `id` | `Long` | `@PrimaryKey(autoGenerate = true)` |
| `amount` | `String` | Количество |
| `unit` | `String` | Единица измерения |
| `drinkId` | `Long` | `@ColumnInfo("drink_id")` |
| `ingredientId` | `Long` | `@ColumnInfo("ingredient_id")` |

Внешние ключи:

- `drink_id` → `Drink.id`
- `ingredient_id` → `Ingredient.id`

Индексы по `drink_id` и `ingredient_id`.

### `WholeCocktail` — проекция (не таблица)

Используется в join-запросах и как модель ингредиента в памяти.

| Поле | Колонка |
|------|---------|
| `jointableId` | `join_id` |
| `ingredientId` | `id` |
| `ingredientName` | `ingredient` |
| `amount` | `amount` |
| `unit` | `unit` |
| `image` | `image` |

### `Taste` — модель (не таблица)

`String id`, `String text`; конструктор `Taste(String text)`.
Хранится внутри `Drink.tastes` как JSON.

### `Glass` (db) — модель (не таблица)

`int glassId`, `String glassName`; `@Embedded` в `Drink`.

### `TasteConverter` (`db/converter`)

Конвертер типа для Room:

- `@TypeConverter ArrayList<Taste> toList(String)` — из JSON;
- `@TypeConverter String toString(ArrayList<Taste>)` — в JSON.

Оба используют Gson.

## DAO (`db/dao`)

### `DrinkDao`

| Метод | Запрос |
|-------|--------|
| `LiveData<Drink> loadDrinkById(Long)` | `SELECT * FROM Drink WHERE id = :id` |
| `LiveData<List<Drink>> loadAllDrinks()` | `SELECT * FROM Drink ORDER BY name ASC` |
| `LiveData<List<Drink>> searchDrinksByName(String)` | `WHERE name LIKE :query ORDER BY name ASC` |
| `Long insertDrink(Drink)` | `@Insert(onConflict = REPLACE)` |
| `void deleteDrinkById(Long)` | `DELETE FROM Drink WHERE id = :drinkId` |

### `IngredientDao`

| Метод | Запрос |
|-------|--------|
| `LiveData<List<Ingredient>> loadIngredients()` | `SELECT * FROM Ingredient` |
| `LiveData<List<Ingredient>> searchIngredientsByName(String)` | `WHERE name LIKE :query` |
| `LiveData<List<WholeCocktail>> loadCocktailIngredients(List<Long>)` | `WHERE Ingredient.id IN (:ingredientIds)` |
| `Ingredient getIngredientByName(String)` | `WHERE name = :name LIMIT 1` |
| `LiveData<Ingredient> loadIngredient(Long)` | `WHERE id = :id` |
| `Long insertOrReplaceIngredient(Ingredient)` | `@Insert(onConflict = REPLACE)` |
| `void deleteIngredientById(Long)` | `DELETE FROM Ingredient WHERE id = :ingredientId` |

### `CocktailDao` (связи)

| Метод | Описание |
|-------|----------|
| `void insertDrinkIngredients(List<DrinkIngredientJoin>)` | Вставка связей (`REPLACE`) |
| `void deleteDrinkIngredientsById(Long)` | Удаление связей напитка |
| `int countCocktailsWithIngridient(Long)` | Сколько коктейлей используют ингредиент |
| `LiveData<List<WholeCocktail>> findAllIngredientsByDrinkId(Long)` | `INNER JOIN Ingredient` по `drink_id` |
| `LiveData<List<Drink>> getDrinksWithIngredient(Long)` | `INNER JOIN` Ingredient и Drink по `ingredient_id` |

## Мапперы (`db/mapper`)

- `Mapper<T1, T2>` — абстрактный базовый класс (`map`, `reverseMap` и списковые версии).
  Объявлен в пакете `sorokinuladzimir.com.homebarassistant.db.mapper`.
- `DrinkEntityToDrinkMapper` — `reverseMap(DrinkEntity) → Drink`: переносит название, вкусы,
  описание, изображение (`ABSOLUT_DRINKS_IMAGE_ROOT + id + ".png"`), оценку, флаги
  алкоголя/газированности и бокал. Синглтон `getInstance()`.
- `WholeCocktailToDrinkIngredientJoinMapper` — `reverseMap(WholeCocktail) → DrinkIngredientJoin`.

## Миграции

Схема имеет `version = 1` и `exportSchema = false`. Миграции не описаны — при изменении
схемы потребуется либо `fallbackToDestructiveMigration()`, либо реализация `Migration`.
