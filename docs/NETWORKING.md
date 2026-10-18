# Сеть (TheCocktailDB API)

Внешние данные о коктейлях берутся из **TheCocktailDB**
(<https://www.thecocktaildb.com/documentation>) через Retrofit. Код — в пакете `net/`.

Источник заменён с Absolut Drinks (сервис прекратил работу) на TheCocktailDB.

## Базовая конфигурация

| Параметр | BuildConfig | Значение по умолчанию |
|----------|-------------|-----------------------|
| Base URL | `COCKTAILDB_ROOT` | `https://www.thecocktaildb.com/api/json/v1/` |
| API-ключ | `COCKTAILDB_API_KEY` | `1` (бесплатный тестовый ключ) |

Итоговый базовый URL: `{COCKTAILDB_ROOT}{COCKTAILDB_API_KEY}/`, например
`https://www.thecocktaildb.com/api/json/v1/1/`. Ключ можно переопределить в `local.properties`:

```properties
COCKTAILDB_API_KEY=1
```

## `CocktailDbApi` (Retrofit-интерфейс)

```kotlin
@GET("search.php")     suspend fun searchByName(@Query("s") name: String): CocktailDbResponse
@GET("search.php")     suspend fun searchByFirstLetter(@Query("f") letter: String): CocktailDbResponse
@GET("lookup.php")     suspend fun lookup(@Query("i") id: String): CocktailDbResponse
@GET("filter.php")     suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailDbResponse
@GET("filter.php")     suspend fun filterByGlass(@Query("g") glass: String): CocktailDbResponse
@GET("filter.php")     suspend fun filterByCategory(@Query("c") category: String): CocktailDbResponse
@GET("filter.php")     suspend fun filterByAlcoholic(@Query("a") alcoholic: String): CocktailDbResponse
```

Все методы — `suspend`.

- `search.php?s=` и `lookup.php?i=` возвращают **полные** записи (с ингредиентами и инструкцией);
- `filter.php` возвращает **краткие** записи (`idDrink`, `strDrink`, `strDrinkThumb`).

## `NetworkModule`

`OkHttpClient` и Retrofit создаются в `core/di/NetworkModule` (`@Singleton`).

- `ConnectivityInterceptor` — проверяет `NetworkUtil.isOnline(context)` и бросает
  `NoConnectivityException` (наследник `IOException`) при отсутствии сети.
- Конвертер — `retrofit2-kotlinx-serialization-converter`
  (`Json { ignoreUnknownKeys = true; coerceInputValues = true }`).

`NetworkUtil.isOnline(Context)` использует `ConnectivityManager` + `NetworkCapabilities`.

## Модели (`net/entity`)

Все `@Serializable`-поля имеют явный `@SerialName`, чтобы R8-обфускация не влияла на
формат данных.

### `CocktailDbResponse`

`@SerialName("drinks") drinks: List<CocktailDbDrink>?` (при отсутствии результатов `null`).

### `CocktailDbDrink` (сырая запись TheCocktailDB)

| Поле (Kotlin) | JSON | Примечание |
|---------------|------|-----------|
| `id` | `idDrink` | |
| `name` | `strDrink` | |
| `category` | `strCategory` | |
| `alcoholic` | `strAlcoholic` | `Alcoholic` / `Non_Alcoholic` / `Optional alcohol` |
| `glass` | `strGlass` | |
| `instructions` | `strInstructions` | |
| `tags` | `strTags` | через запятую |
| `imageUrl` | `strDrinkThumb` | |
| `ingredient1..15` | `strIngredient1..15` | |
| `measure1..15` | `strMeasure1..15` | |
| `ingredientLines()` | — | собирает пары имя/мера, пропуская `null` |

Остальные поля ответа игнорируются (`ignoreUnknownKeys = true`).

### `IngredientDto`

`name`, `measure` — строка ингредиента для быстрого отображения и сохранения.

### `DrinkEntity` (модель приложения)

`id`, `name`, `description`, `color`, `rating`, `isAlcoholic`, `isCarbonated`, `servedIn`,
`tastes`, `imageUrl`, `ingredients`.

### `Glass` / `TasteDto`

`Glass(text, name, image)`, `TasteDto(id, text)`.

### `CocktailDbDrinkMapper`

`CocktailDbDrink → DrinkEntity`:

- `description = strInstructions`;
- `isAlcoholic = strAlcoholic == "Alcoholic"`;
- `servedIn = Glass(text = strGlass)`;
- `tastes = strTags` (разбитые по запятой; мусорные/код-подобные теги отфильтровываются);
- `imageUrl = strDrinkThumb`;
- `ingredients = ingredientLines()`;
- `color = null`, `rating = 0`, `isCarbonated = false` (нет в источнике).

## Условия поиска (`SearchConditions`)

Экран фильтров (`ui/search/SearchDrinksScreen`) собирает `SearchConditions` и передаёт его
сериализованным в JSON через маршрут `found_drinks?conditions=...`. Прямой поиск использует
одно условие; AI-поиск формирует `SearchRequest` из 1–3 условий. Репозиторий применяет
**первое подходящее** условие каждого запроса (API поддерживает только один фильтр за
запрос), а результаты объединяет по `idDrink`:

| Приоритет | Поле | Запрос |
|-----------|------|--------|
| 1 | `name` | `search.php?s=` |
| 2 | `ingredient` | `filter.php?i=` |
| 3 | `glass` | `filter.php?g=` |
| 4 | `category` | `filter.php?c=` |
| 5 | `alcoholic` | `filter.php?a=Alcoholic` / `Non_Alcoholic` |

### Игнорируемые параметры

Следующих полей нет в TheCocktailDB: `taste`, `carbonated`, `color`, `skill`.
Прямой поиск («ИСКАТЬ») их не использует, но **AI-поиск** передаёт их локальному LLM как
подсказки, и модель может «додумать» по ним `name`/`category`/`ingredient`
(см. [AI.md](AI.md)).

## Поток данных

1. **Поиск** (`FoundDrinksScreen` → `FoundDrinksViewModel.searchDrinks` →
   `BarDataRepository.getRemoteDrinks`): выбор эндпоинта по `SearchConditions`, результат —
   `List<DrinkEntity>` (для `filter.php` — краткие записи).
2. **Детали** (клик по элементу → маршрут `remote_drink/{drinkId}` →
   `RemoteDrinkViewModel` → `BarDataRepository.getRemoteDrink(id)`): `lookup.php?i=` даёт
   полную запись (ингредиенты + инструкция).
3. **Сохранение** (`RemoteDrinkViewModel.saveDrink` →
   `BarDataRepository.saveRemoteDrink(DrinkEntity, Bitmap)`):
   1. `DrinkEntityToDrinkMapper.reverseMap(...)` → `Drink`;
   2. изображение: если получен `Bitmap` — сохраняется в app-private файл, иначе используется
      `imageUrl`;
   3. `DrinkDao.insertDrink(...)`;
   4. ингредиенты → `DrinkIngredientJoin` (поиск/создание ингредиента по имени,
      `amount = measure`) → `CocktailDao.insertDrinkIngredients(...)`.

## Изображения

- Коктейли: `strDrinkThumb` (можно добавить `/small`, `/medium`, `/large`).
- Ингредиенты: `https://www.thecocktaildb.com/images/ingredients/{Name}.png`.

## Ограничения

- Бесплатный доступ ограничен одним фильтром за запрос (нет мульти-ингредиентов и фильтров
  «популярное/последние» — это Premium).
- Нет рейтинга, вкуса, цвета, крепости, признака газированности и пагинации — соответствующие
  фильтры UI пока игнорируются.
- Поле `rating` в карточках локальных напитков остаётся (собственная оценка пользователя).
- Доступность API зависит от TheCocktailDB; локальные функции (коллекция, свои рецепты)
  работают без сети.
