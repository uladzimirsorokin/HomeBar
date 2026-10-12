# Архитектура

Приложение построено по шаблону **MVVM** с одним Activity-хостом (`MainActivity`) и вложенными
фрагментами. Слои разделены на **UI → ViewModel → Repository → (Room | Retrofit)**.
Dependency Injection отсутствует — все зависимости раздаются через статические синглтоны в `BarApp`.

## Слои

```
┌─────────────────────────────────────────────────────────────┐
│  UI (Fragments / Adapters)                                  │
│  observe LiveData, отправляют команды через ViewModel       │
└───────────────────────────┬─────────────────────────────────┘
                            │ LiveData / методы
┌───────────────────────────▼─────────────────────────────────┐
│  ViewModel (viewmodel/)                                     │
│  AndroidViewModel, готовят LiveData для экранов             │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  BarDataRepository (implements BarData)                     │
│  единый источник данных, MediatorLiveData, пагинация        │
└───────────────┬───────────────────────────┬─────────────────┘
                │                           │
      ┌─────────▼─────────┐       ┌─────────▼─────────┐
      │  Room (db/)       │       │  Retrofit (net/)  │
      │  CocktailsDatabase│       │  AbsolutDrinksApi │
      └───────────────────┘       └───────────────────┘
                │                           │
      ┌─────────▼───────────────────────────▼─────────┐
      │  AppExecutors: diskIO / networkIO / mainThread │
      └───────────────────────────────────────────────┘
```

### `BarApp` — точка сборки

`BarApp extends Application` создаёт и хранит глобальные объекты:

- `Cicerone<Router>` — навигация (`getNavigatorHolder()`, `getRouter()`);
- `AppExecutors` — пулы потоков;
- `Context` приложения и язык системы (`getLang()`).

Статические аксессоры: `getInstance()`, `getAppContext()`, `getDatabase()`,
`getBarRepository()`, `getExecutors()`.

### `AppExecutors`

| Метод | Реализация |
|-------|------------|
| `diskIO()` | `Executors.newSingleThreadExecutor()` |
| `networkIO()` | `Executors.newFixedThreadPool(3)` |
| `mainThread()` | `MainThreadExecutor` на `Handler(Looper.getMainLooper())` |

### Репозиторий

`BarData` — интерфейс-контракт (CRUD для напитков и ингредиентов, работа с изображениями,
запросы к API). `BarDataRepository` — потокобезопасный синглтон (`SingletonHolder`), который:

- держит `MediatorLiveData` для локальных напитков, ингредиентов, удалённых напитков
  (`List<DrinkEntity>`) и удалённых ингредиентов (`List<WholeCocktail>`);
- хранит `LiveData<String>` для путей к фото напитка/ингредиента;
- выполняет работу с БД и сетью через `AppExecutors`;
- реализует пагинацию через `enum QueryType { SEARCH_BY_NAME, SEARCH_BY_CONDITIONS, CURRENT }`
  и поля `mTotalResult`, `mNextLink`, `mCurrentSearchType`.

### ViewModels

| ViewModel | Назначение |
|-----------|------------|
| `DrinkListViewModel` | Список локальных напитков + поиск по названию |
| `IngredientListViewModel` | Список локальных ингредиентов + поиск |
| `DrinkViewModel` | Локальный напиток + его ингредиенты (экран `LocalDrinkFragment`) |
| `IngredientViewModel` | Локальный ингредиент + связанные напитки |
| `AddDrinkViewModel` | Создание/редактирование напитка (ингредиенты, вкусы, фото, оценка) |
| `AddDrinkIngredientsViewModel` | Выбор ингредиентов для напитка (поиск, множественный выбор) |
| `AddIngredientViewModel` | Создание/редактирование ингредиента |
| `RemoteDrinkViewModel` | Детали удалённого напитка, сохранение в локальную БД |
| `FoundDrinksViewModel` | Результаты удалённого поиска + пагинация |
| `SharedViewModel` | Общий выбор ингредиентов между `AddDrinkFragment` и `AddDrinkIngredientsFragment` |

Часть ViewModel предоставляет собственную `Factory` для передачи `id` сущности.

## Навигация (Cicerone)

Навигация двухуровневая:

1. **Глобальный роутер** (`BarApp.getRouter()`) — переключает нижние вкладки через
   `MainActivity` (`Screens.SEARCH`, `Screens.DRINKS`, `Screens.INGREDIENTS`).
2. **Локальные роутеры** — по одному на вкладку, хранятся в `LocalCiceroneHolder`
   (ключ — имя контейнера). Внутри вкладки используется `SupportFragmentNavigator`
   в `TabContainerFragment` (контейнер `R.id.ftc_container`).

`MainActivity`:

- `initBottomNavigation()` — 3 пункта `AHBottomNavigation` → `router.replaceScreen(...)`;
- `initContainers()` — создаёт и отсоединяет три `TabContainerFragment`;
- глобальный `Navigator` обрабатывает `Back` (`finish()`), `SystemMessage` (Toast) и
  `Replace` (attach/detach вкладок через `commitNow()`);
- `onBackPressed()` делегирует видимому фрагменту (`BackButtonListener`) либо `router.exit()`.

`TabContainerFragment` выбирает корневой экран вкладки:

| Вкладка | Корневой экран |
|---------|----------------|
| `SEARCH` | `FOUND_DRINKS` |
| `DRINKS` | `DRINKS_LIST` |
| `INGREDIENTS` | `INGREDIENTS_LIST` |

`ui.subnavigation`:

- `BackButtonListener` — `boolean onBackPressed()`;
- `RouterProvider` — `Router getRouter()`;
- `LocalCiceroneHolder` — `HashMap<String, Cicerone<Router>>` по имени контейнера.

Полная карта экранов и ключей — в [UI.md](UI.md).

## Работа с изображениями

`ImageHandler` (`ui/utils`) инкапсулирует файловые операции:

- `createImageFile()` — `IMG_yyyyMMdd_HHmmss_<random>.jpg`;
- `getBitmapFromUri(...)` — чтение с масштабированием (`inSampleSize`);
- `saveImage(Bitmap)` — сохранение PNG в `Pictures/HomeBar`;
- `createImageFile(Context)` — URI через `FileProvider` (authority `Constants.Strings.AUTHORITY`);
- `deleteImage(...)`, `isExternalStorageWritable()`, `getAlbumStorageDir()`.

Доступ к камере/галерее реализован через `AddImageDialogFragment` и
`ActivityResult`/`requestPermissions` в `AddDrinkFragment` и `AddIngredientFragment`.

## Особенности и ограничения

- Нет DI (Dagger/Hilt) — вся проводка через синглтоны `BarApp`.
- Используются легаси-API: `ViewModelProviders.of(...)`, `getFragmentManager()`,
  `setTargetFragment(...)`, `WRITE_EXTERNAL_STORAGE` (целевой `targetSdk 35`).
- Базовый класс `Mapper` объявлен в пакете `sorokinuladzimir.com.homebarassistant.db.mapper`,
  хотя файл лежит в `.../db/mapper/`.
- `LocalDrinkViewModel` отсутствует — экран локального напитка использует `DrinkViewModel`.
