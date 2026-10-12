# Сеть (Absolut Drinks API)

Внешние данные о коктейлях берутся из **Absolut Drinks Database (ADDB)** через Retrofit.
Код сосредоточен в пакете `net/`.

## Базовая конфигурация

Константы в `Constants.java`:

| Константа | Значение |
|-----------|----------|
| `Constants.Uri.ABSOLUT_DRINKS_ROOT` | `http://addb.absolutdrinks.com` |
| `Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT` | `http://assets.absolutdrinks.com/drinks/600x800/` |
| `Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT` | `http://assets.absolutdrinks.com/` |
| `Constants.Keys.ABSOLUT_API_KEY` | API-ключ (query-параметр `apiKey`) |
| `Constants.Values.DEFAULT_ITEM_AMOUNT` | `5` (размер страницы) |
| `Constants.Values.DEFAULT_IMAGE_SIZE` | `750` |

## `AbsolutDrinksApi` (Retrofit-интерфейс)

```java
@GET("/drinks/{conditions}")
Call<AbsolutDrinksResult> getAllMatchedDrinks(
        @Path("conditions") String conditions,
        @Query("start") int start,
        @Query("pageSize") int pageSize);

@GET("quickSearch/drinks/{searchString}")
Call<AbsolutDrinksResult> searchDrinks(
        @Path("searchString") String searchString,
        @Query("start") int start,
        @Query("pageSize") int pageSize);

@GET("/drinks/{drinkId}/howtomix")
Call<Preparation> getPreparationSteps(@Path("drinkId") String drinkId);
```

- `getAllMatchedDrinks` — поиск по условиям (`conditions` — путь, собирается `DrinksPathManager`);
- `searchDrinks` — быстрый поиск по строке;
- `getPreparationSteps` — шаги приготовления и ингредиенты напитка.

## `ServiceGenerator`

Синглтон, создающий `OkHttpClient` и Retrofit-сервис.

**Интерцепторы:**

- `ConnectivityInterceptor` — проверяет `NetworkUtil.isOnline(context)` и бросает
  `NoConnectivityException` (наследник `IOException`, сообщение `"No internet connection"`)
  при отсутствии сети.
- `DrinksRequestInterceptor(lang)` — добавляет к каждому запросу `apiKey` и `lang`
  (`"ru"` для русского языка системы, иначе `"en"`).

Конвертер — `GsonConverterFactory.create()`.

`NetworkUtil.isOnline(Context)` использует `ConnectivityManager.getActiveNetworkInfo()`.

## Модели (`net/entity`)

### `AbsolutDrinksResult`

| Поле | Тип |
|------|-----|
| `result` | `List<DrinkEntity>` |
| `totalResult` | `int` |
| `next` | `String` (ссылка для пагинации) |

### `DrinkEntity` (удалённый напиток)

| Поле | JSON | Тип |
|------|------|-----|
| `id` | | `String` |
| `name` | | `String` |
| `description` | `descriptionPlain` | `String` |
| `color` | | `String` |
| `rating` | | `int` |
| `isAlcoholic` | | `boolean` |
| `isCarbonated` | | `boolean` |
| `servedIn` | `servedIn` | `Glass` |
| `tastes` | | `ArrayList<Taste>` |

### `Glass` (net)

`String text`, `String name`, `int image`.

### `Preparation`

`@SerializedName("steps") ArrayList<PreparationStep> preparationSteps`.

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
- `CURRENT` → повтор текущего запроса (например, pull-to-refresh).

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

`build()` собирает финальный путь, `getPath()` возвращает его. Результат передаётся в
`FoundDrinksFragment` через `Constants.Extra.REQUEST_CONDITIONS`.

## Разбор ингредиентов

Ответ `Preparation` преобразуется в `List<WholeCocktail>` в
`BarDataRepository.handlePreparation(Preparation)` и `matchAmountAndUnit(...)`,
после чего доступен `RemoteDrinkViewModel.mDrinkIngredients`.

## Сохранение удалённого напитка

`RemoteDrinkViewModel.saveDrink()` → `BarDataRepository.saveRemoteDrink(DrinkEntity, Bitmap)`:

1. `DrinkEntityToDrinkMapper.reverseMap(...)` → `Drink`;
2. сохранение изображения через `ImageHandler.saveImage(...)`;
3. `DrinkDao.insertDrink(...)`;
4. построение `DrinkIngredientJoin` (сопоставление ингредиентов по имени через
   `getIngredientByName` / `insertOrReplaceIngredient`);
5. `CocktailDao.insertDrinkIngredients(...)`.

## Ограничения

- API-ключ захардкожен в `Constants.Keys.ABSOLUT_API_KEY`.
- Используется `http://` (не HTTPS).
- Доступность внешнего API зависит от сервиса Absolut Drinks; локальные функции
  (коллекция, свои рецепты) работают без сети.
