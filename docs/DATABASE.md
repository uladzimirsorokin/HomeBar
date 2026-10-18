# База данных (Room)

Локальное хранилище реализовано на **Room 2.6.1** (Kotlin, KSP).
Класс-холдер: `db/CocktailsDatabase.kt`.

```kotlin
@Database(
    entities = [Drink::class, Ingredient::class, DrinkIngredientJoin::class],
    version = 1,
    exportSchema = false
)
abstract class CocktailsDatabase : RoomDatabase()
```

- Имя файла БД: `cocktails_db`
- Инстанс создаётся в `DatabaseModule` (`@Singleton`, `Room.databaseBuilder`) и раздаётся Hilt.
- DAO: `getDrinkDao()`, `getIngredientDao()`, `getCocktailDao()`.
- Чтение — `Flow<...>`, запись — `suspend`-функции; во ViewModel мостится через `asLiveData()`.

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
| `id` | `Long?` | `@PrimaryKey(autoGenerate = true)` |
| `name` | `String?` | Название |
| `description` | `String?` | Описание |
| `tastes` | `ArrayList<Taste>?` | Вкусы (JSON через `TasteConverter`) |
| `image` | `String?` | Путь/URL к изображению |
| `glass` | `@Embedded Glass?` | Бокал (встроенный объект) |
| `rating` | `Int` | Оценка |
| `isAlcoholic` | `Boolean` | Алкогольный |
| `isCarbonated` | `Boolean` | Газированный |
| `notes` | `String?` | Заметки |

Индекс: уникальный по `id`. Аннотация `@TypeConverters(TasteConverter::class)`.

### `Ingredient` — `@Entity`

| Поле | Тип |
|------|-----|
| `id` | `Long?` (`@PrimaryKey(autoGenerate = true)`) |
| `name` | `String?` |
| `description` | `String?` |
| `image` | `String?` |
| `notes` | `String?` |

Уникальный индекс по `id`. Переопределены `equals`/`hashCode`/`toString`.

### `DrinkIngredientJoin` — `@Entity` (связь)

| Поле | Тип | Примечание |
|------|-----|-----------|
| `id` | `Long?` | `@PrimaryKey(autoGenerate = true)` |
| `amount` | `String?` | Количество |
| `unit` | `String?` | Единица измерения |
| `drinkId` | `Long?` | `@ColumnInfo("drink_id")` |
| `ingredientId` | `Long?` | `@ColumnInfo("ingredient_id")` |

Внешние ключи: `drink_id → Drink.id`, `ingredient_id → Ingredient.id`.
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

`String id`, `String text`; конструктор `Taste(String text)`. Аннотирована `@Serializable`,
хранится внутри `Drink.tastes` как JSON.

### `Glass` (db) — модель (не таблица)

`Int glassId`, `String glassName`; `@Embedded` в `Drink`.

### `TasteConverter` (`db/converter`)

Конвертер типа для Room на **kotlinx.serialization**:

- `@TypeConverter toList(String?): ArrayList<Taste>?` — из JSON;
- `@TypeConverter fromList(ArrayList<Taste>?): String?` — в JSON.

## DAO (`db/dao`)

### `DrinkDao`

| Метод | Тип |
|-------|-----|
| `loadDrinkById(id: Long?)` | `Flow<Drink?>` |
| `loadAllDrinks()` | `Flow<List<Drink>>` (`ORDER BY name ASC`) |
| `searchDrinksByName(query)` | `Flow<List<Drink>>` (`name LIKE :query`) |
| `insertDrink(drink)` | `suspend`, `@Insert(REPLACE)` |
| `deleteDrinkById(id)` | `suspend` |

### `IngredientDao`

| Метод | Тип |
|-------|-----|
| `loadIngredients()` | `Flow<List<Ingredient>>` |
| `searchIngredientsByName(query)` | `Flow<List<Ingredient>>` |
| `loadCocktailIngredients(ids)` | `Flow<List<WholeCocktail>>` |
| `getIngredientByName(name)` | `suspend Ingredient?` |
| `loadIngredient(id)` | `Flow<Ingredient?>` |
| `insertOrReplaceIngredient(ingredient)` | `suspend Long` |
| `deleteIngredientById(id)` | `suspend` |

### `CocktailDao` (связи)

| Метод | Тип |
|-------|-----|
| `insertDrinkIngredients(list)` | `suspend` (`REPLACE`) |
| `deleteDrinkIngredientsById(id)` | `suspend` |
| `countCocktailsWithIngridient(id)` | `suspend Int` |
| `findAllIngredientsByDrinkId(id)` | `Flow<List<WholeCocktail>>` (INNER JOIN) |
| `getDrinksWithIngredient(id)` | `Flow<List<Drink>>` (INNER JOIN) |

## Мапперы (`db/mapper`)

- `Mapper<T1, T2>` — абстрактный базовый класс (`map`, `reverseMap` и списковые версии).
- `DrinkEntityToDrinkMapper` — `reverseMap(DrinkEntity) → Drink`: переносит название, вкусы,
  описание, изображение (`imageUrl` из TheCocktailDB), оценку, флаги
  алкоголя/газированности и бокал. Синглтон `getInstance()`.
- `WholeCocktailToDrinkIngredientJoinMapper` — `reverseMap(WholeCocktail) → DrinkIngredientJoin`.

## Миграции

Схема имеет `version = 1` и `exportSchema = false`. Миграции не описаны — при изменении
схемы потребуется либо `fallbackToDestructiveMigration()`, либо реализация `Migration`.
