# Архитектура

Приложение построено по шаблону **MVVM** на **Kotlin + Jetpack Compose**. Единственная
Activity-хост — `MainActivity` с Compose `NavHost`; экраны — `@Composable`-функции.
Зависимости раздаются через **Hilt**. Слои: **UI → ViewModel → Repository → (Room | Retrofit)**.

## Слои

```
┌─────────────────────────────────────────────────────────────┐
│  UI (Compose)                                               │
│  @Composable-экраны, observe LiveData/StateFlow             │
└───────────────────────────┬─────────────────────────────────┘
                            │ LiveData / StateFlow
┌───────────────────────────▼─────────────────────────────────┐
│  ViewModel (viewmodel/)                                     │
│  @HiltViewModel, AndroidViewModel, готовят LiveData         │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  BarDataRepository (implements BarData)                     │
│  единый источник данных, MediatorLiveData, пагинация,       │
│  Coroutines (Dispatchers.IO)                                 │
└───────────────┬───────────────────────────┬─────────────────┘
                │                           │
      ┌─────────▼─────────┐       ┌─────────▼─────────┐
      │  Room (db/)       │       │  Retrofit (net/)  │
      │  CocktailsDatabase│       │  AbsolutDrinksApi │
      │  Flow / suspend   │       │  suspend          │
      └───────────────────┘       └───────────────────┘
```

## DI (Hilt)

`BarApp` (`@HiltAndroidApp`) — точка входа Hilt. Модули в `core/di`:

| Модуль | Предоставляет |
|--------|---------------|
| `DatabaseModule` | `CocktailsDatabase` (`@Singleton`), `DrinkDao`, `IngredientDao`, `CocktailDao` |
| `NetworkModule` | `AbsolutDrinksApi` (`@Singleton`) — OkHttp с интерцепторами + Retrofit на kotlinx.serialization |
| `RepositoryModule` | `@Binds BarDataRepository → BarData` (`@Singleton`) |
| `AppModule` | `ImageHandler` |

Activity и ViewModel помечены `@AndroidEntryPoint` / `@HiltViewModel`, зависимости
инжектируются конструктором. Статических синглтонов данных в `BarApp` больше нет.

## Репозиторий

`BarData` — интерфейс-контракт (CRUD для напитков и ингредиентов, работа с изображениями,
запросы к API). `BarDataRepository` (`@Singleton`, инжектируется Hilt):

- держит `MediatorLiveData` для локальных напитков/ингредиентов, удалённых напитков
  (`List<DrinkEntity>`) и удалённых ингредиентов (`List<WholeCocktail>`);
- хранит `MediatorLiveData<String?>` для путей к фото напитка/ингредиента;
- выполняет работу с БД/сетью в `CoroutineScope(SupervisorJob() + Dispatchers.IO)`;
- DAO возвращают `Flow`, которые мостятся во ViewModel через `asLiveData()`;
- реализует пагинацию через `enum QueryType { SEARCH_BY_NAME, SEARCH_BY_CONDITIONS, CURRENT }`
  и поля `mTotalResult`, `mNextLink`, `mCurrentSearchType`;
- сетевые ошибки оборачивает в `ApiResult` (Success/Error).

## ViewModels

| ViewModel | Назначение |
|-----------|------------|
| `DrinkListViewModel` | Список локальных напитков + поиск по названию |
| `IngredientListViewModel` | Список локальных ингредиентов + поиск |
| `DrinkViewModel` | Локальный напиток + его ингредиенты |
| `IngredientViewModel` | Локальный ингредиент + связанные напитки |
| `AddDrinkViewModel` | Создание/редактирование напитка (ингредиенты, вкусы, фото, оценка) |
| `AddDrinkIngredientsViewModel` | Выбор ингредиентов для напитка (поиск, множественный выбор) |
| `AddIngredientViewModel` | Создание/редактирование ингредиента |
| `RemoteDrinkViewModel` | Детали удалённого напитка, сохранение в локальную БД |
| `FoundDrinksViewModel` | Результаты удалённого поиска + пагинация |
| `SharedViewModel` | Общий выбор ингредиентов между экранами формы напитка |
| `ThemeViewModel` | Тема приложения (DataStore → `StateFlow<Int>`) |

Аргументы экранов передаются через `SavedStateHandle` (id сущности, JSON удалённого напитка).

## Навигация (Navigation Compose)

Маршруты описаны в `ui/navigation/Routes.kt`, граф — в `homeBarGraph(...)`
(`HomeBarNavHost.kt`), хост — `HomeBarApp()`:

- `Scaffold` с `NavigationBar` (Material 3) для трёх вкладок: Поиск, Коктейли, Ингредиенты;
- `NavHost` с `startDestination = DRINKS_LIST`;
- вложенные экраны: детали, формы, выбор ингредиентов, About, Settings;
- состояние нижней вкладки выводится из текущего route.

Полная карта экранов и ключей — в [UI.md](UI.md).

## Работа с изображениями

`ImageHandler` (`ui/utils`) инкапсулирует файловые операции:

- `createImageFile(Context)` — временный JPEG в app-private
  `getExternalFilesDir(Pictures)/HomeBar`, возвращает `FileProvider` URI;
- `getBitmapFromUri(...)` — чтение с масштабированием (`inSampleSize`);
- `saveImage(context, Bitmap)` — сохранение PNG в app-private директорию;
- `deleteImage(...)`.

Запрос фото из камеры/галереи реализован через **Activity Result API**
(`rememberImagePicker` → `TakePicture` / `GetContent`). Публичное хранилище и
`WRITE_EXTERNAL_STORAGE` не используются.

## Тема

Тема хранится в **DataStore** (`core/data/ThemePreferences`, ключ `app_theme`).
`ThemeViewModel` отдаёт `StateFlow<Int>`; `MainActivity`/`ResourcesActivity` собирают его
через `collectAsStateWithLifecycle()` и переключают `HomeBarTheme` без перезапуска Activity.
Три схемы (`ThemeVariant`: INDIGO/PURPLE/BLACK) определены в `core/ui/theme`.
