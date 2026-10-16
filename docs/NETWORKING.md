# Сеть (Absolut Drinks API)

Внешние данные о коктейлях берутся из **Absolut Drinks Database (ADDB)** через Retrofit.
Код сосредоточен в пакете `net/`.

## Базовая конфигурация

URL и ключ задаются в `app/build.gradle` через `BuildConfig` (значения по умолчанию,
переопределяются в `local.properties`) и доступны через `Constants.kt`:

| Константа | BuildConfig | Значение по умолчанию |
|-----------|-------------|-----------------------|
| `Constants.Uri.ABSOLUT_DRINKS_ROOT` | `ABSOLUT_DRINKS_ROOT` | `http://addb.absolutdrinks.com` |
| `Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT` | `ABSOLUT_DRINKS_IMAGE_ROOT` | `http://assets.absolutdrinks.com/drinks/600x800/` |
| `Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT` | `ABSOLUT_INGREDIENTS_IMAGE_ROOT` | `http://assets.absolutdrinks.com/` |
| `Constants.Keys.ABSOLUT_API_KEY` | `ABSOLUT_API_KEY` | API-ключ (query-параметр `apiKey`) |
| `Constants.Values.DEFAULT_ITEM_AMOUNT` | — | `5` (размер страницы) |

## `AbsolutDrinksApi` (Retrofit-интерфейс)

```kotlin
@GET("/drinks/{conditions}")
suspend fun getAllMatchedDrinks(
    @Path("conditions") conditions: String,
    @Query("start") start: Int,
    @Query("pageSize") pageSize: Int
): AbsolutDrinksResult

@GET("quickSearch/drinks/{searchString}")
suspend fun searchDrinks(
    @Path("searchString") searchString: String,
    @Query("start") start: Int,
    @Query("pageSize") pageSize: Int
): AbsolutDrinksResult

@GET("/drinks/{drinkId}/howtomix")
suspend fun getPreparationSteps(@Path("drinkId") drinkId: String): Preparation
```

Все методы — `suspend` (Retrofit поддерживает корутины).

## `NetworkModule`

`OkHttpClient` и Retrofit создаются в `core/di/NetworkModule` (`@Singleton`).

**Интерцепторы:**

- `ConnectivityInterceptor` — проверяет `NetworkUtil.isOnline(context)` и бросает
  `NoConnectivityException` (наследник `IOException`) при отсутствии сети.
- `DrinksRequestInterceptor(lang)` — добавляет к каждому запросу `apiKey` и `lang`
  (`"ru"` для русского языка системы, иначе `"en"`).

**Конвертер** — `retrofit2-kotlinx-serialization-converter`
(`Json { ignoreUnknownKeys = true; coerceInputValues = true }`).

`NetworkUtil.isOnline(Context)` использует `ConnectivityManager` + `NetworkCapabilities`.

## Модели (`net/entity`)

Все `@Serializable`-поля имеют явный `@SerialName`, чтобы R8-обфускация не влияла на
формат данных.

### `AbsolutDrinksResult`

| Поле | Тип |
|------|-----|
| `result` | `List<DrinkEntity>?` |
| `totalResult` | `Int` |
| `next` | `String?` (ссылка для пагинации) |

### `DrinkEntity` (удалённый напиток)

| Поле | JSON |
|------|------|
| `id` | `id` |
| `name` | `name` |
| `description` | `descriptionPlain` |
| `color` | `color` |
| `rating` | `rating` |
| `isAlcoholic` | `isAlcoholic` |
| `isCarbonated` | `isCarbonated` |
| `servedIn` | `servedIn` |
| `tastes` | `tastes` |

### `Glass` (net)

`text`, `name`, `image`.

### `TasteDto`

`id`, `text`.

### `Preparation`

`@SerialName("steps") preparationSteps: List<PreparationStep>?`.

### `PreparationStep`

| Поле | JSON |
|------|------|
| `ingredientDescription` | `description` |
| `imagePath` | `imagePath` |
| `imageName` | `imageName` |
| `amount` | `quantity` |
| `centilitresAmount` | `centilitres` |
| `unit` | `measurement` |
| `ingredientName` | `ingredient` |
| `preparationStepText` | `textPlain` |

## Пагинация и типы запросов

`BarDataRepository` использует `enum QueryType`:

- `SEARCH_BY_NAME` → `AbsolutDrinksApi.searchDrinks`;
- `SEARCH_BY_CONDITIONS` → `AbsolutDrinksApi.getAllMatchedDrinks`;
- `CURRENT` → повтор текущего запроса (например, load-more).

Состояние пагинации: `mTotalResult`, `mNextLink`, `mCurrentSearchType`.
Следующая страница запрашивается с `start += pageSize` (`pageSize = DEFAULT_ITEM_AMOUNT = 5`).

## Построение условий поиска

`ui/utils/DrinksPathManager` — immutable-builder для параметра `conditions`:

| Метод Builder | Фрагмент пути |
|---------------|---------------|
| `setGlassType(...)` | `servedin/...` |
| `setTaste(...)` | `tasting/.../and/...` |
| `setRating(min, max)` | `rating/gteX/and/lteY` |
| `setColor(...)` | `colored/...` |
| `setIngredient(...)` | `withtype/.../and/...` |
| `setSkill(...)` | `skill/.../or/...` |
| `setCarbonated(...)` | `carbonated` / `not/carbonated` |
| `setAlcoholic(...)` | `alcoholic` / `not/alcoholic` |

`build().path` собирает финальный путь. Результат передаётся в маршрут
`found_drinks?conditions=...`.

## Разбор ингредиентов

Ответ `Preparation` преобразуется в `List<WholeCocktail>` в
`BarDataRepository.handlePreparation(Preparation)` и `matchAmountAndUnit(...)`,
после чего доступен через `RemoteDrinkViewModel`.

## Сохранение удалённого напитка

`RemoteDrinkViewModel.saveDrink()` → `BarDataRepository.saveRemoteDrink(DrinkEntity, Bitmap)`:

1. `DrinkEntityToDrinkMapper.reverseMap(...)` → `Drink`;
2. сохранение изображения через `ImageHandler.saveImage(context, ...)`;
3. `DrinkDao.insertDrink(...)`;
4. построение `DrinkIngredientJoin` (сопоставление ингредиентов по имени через
   `getIngredientByName` / `insertOrReplaceIngredient`);
5. `CocktailDao.insertDrinkIngredients(...)`.

## Ограничения

- API-ключ по умолчанию захардкожен в `build.gradle` (можно переопределить в `local.properties`).
- Используется `http://` (не HTTPS).
- Доступность внешнего API зависит от сервиса Absolut Drinks; локальные функции
  (коллекция, свои рецепты) работают без сети.
