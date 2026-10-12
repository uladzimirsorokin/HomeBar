# UI и навигация

## Точки входа

| Компонент | Роль |
|-----------|------|
| `SplashActivity` | Launcher, показывает заставку и сразу открывает `MainActivity` |
| `MainActivity` | Хост нижней навигации и трёх вкладок |
| `ResourcesActivity` | Экран «Ресурсы» (ссылки на Freepik и Absolut Drinks) |

## Карта экранов

Ключи экранов заданы в `ui/fragments/Screens.java`. Три корневых ключа обрабатываются
на уровне `MainActivity`, остальные — вложенными `SupportFragmentNavigator` внутри вкладок.

### Корневые вкладки (уровень Activity)

| Ключ | Значение | Экран |
|------|----------|-------|
| `SEARCH` | `"search screen"` | `TabContainerFragment` (поиск) |
| `DRINKS` | `"drinks screen"` | `TabContainerFragment` (мои коктейли) |
| `INGREDIENTS` | `"ingredients screen"` | `TabContainerFragment` (ингредиенты) |

### Экраны внутри вкладок

| Ключ | Значение | Фрагмент |
|------|----------|----------|
| `FOUND_DRINKS` | `"found drinks screen"` | `FoundDrinksFragment` |
| `SEARCH_DRINKS` | `"search drinks screen"` | `SearchDrinksFragment` |
| `DRINKS_LIST` | `"drinks list screen"` | `DrinksListFragment` |
| `INGREDIENTS_LIST` | `"ingredients list screen"` | `IngredientsListFragment` |
| `SINGLE_DRINK` | `"single drink screen"` | `RemoteDrinkFragment` |
| `LOCAL_DRINK` | `"local drink screen"` | `LocalDrinkFragment` |
| `LOCAL_INGREDIENT` | `"local ingredient screen"` | `IngredientFragment` |
| `ADD_DRINK` | `"add drink screen"` | `AddDrinkFragment` |
| `ADD_INGREDIENT` | `"add ingredient screen"` | `AddIngredientFragment` |
| `ADD_DRINK_INGREDIENTS` | `"add drink ingredients screen"` | `AddDrinkIngredientsFragment` |
| `ABOUT` | `"about screen"` | `AboutFragment` |
| `SETTINGS` | `"settings screen"` | `SettingsFragment` |

### Схема потоков

```
MainActivity
├── [Поиск]   TabContainerFragment
│              └── FoundDrinksFragment ──► SearchDrinksFragment ──► (FOUND_DRINKS)
│                     └── RemoteDrinkFragment ──(Add)──► локальная БД
│                     └── AboutFragment / SettingsFragment
├── [Коктейли] TabContainerFragment
│              └── DrinksListFragment ──► LocalDrinkFragment ──(Edit)──► AddDrinkFragment
│                     └── AddDrinkIngredientsFragment
│                     └── AboutFragment / SettingsFragment
└── [Ингредиенты] TabContainerFragment
               └── IngredientsListFragment ──► IngredientFragment ──(Edit)──► AddIngredientFragment
                      └── AboutFragment / SettingsFragment
```

## Фрагменты (`ui/fragments`)

### Списки и поиск

- **`FoundDrinksFragment`** — стартовый экран вкладки «Поиск». Показывает найденные из API
  коктейли (`DrinkCardItemAdapter`), поддерживает `SwipeRefreshLayout`, поиск по названию,
  пагинацию (load-more), переход к деталям и к расширенному поиску (FAB).
  Меню: «О приложении», «Настройки».
- **`SearchDrinksFragment`** — расширенный фильтр: бокал, вкус, ингредиент (spinner),
  газированность/алкогольность (switch), цвет (Spectrum), навык (`SeekBar`).
  Собирает путь через `DrinksPathManager` и открывает `FOUND_DRINKS` с условиями.
- **`DrinksListFragment`** — список локальных коктейлей (`LocalDrinksListAdapter`) с
  алфавитным скроллбаром (`MaterialScrollBar`), поиском и FAB создания.
- **`IngredientsListFragment`** — список локальных ингредиентов (`IngredientsItemAdapter`)
  с алфавитным скроллбаром, поиском и FAB создания.

### Детали

- **`RemoteDrinkFragment`** — детали коктейля из API (CollapsingToolbar). Загрузка фото через
  Glide, список ингредиентов (`RemoteDrinkIngredientItemAdapter`), кнопка «Добавить»
  сохраняет рецепт в локальную БД.
- **`LocalDrinkFragment`** — детали локального коктейля (`LocalDrinkIngredientItemAdapter`).
  При `EDITABLE` доступно редактирование; клик по ингредиенту открывает `LOCAL_INGREDIENT`.
- **`IngredientFragment`** — деталь локального ингредиента: описание, заметки и список
  связанных коктейлей (`DrinkSimpleItemAdapter`). При `EDITABLE` — редактирование.

### Редактирование

- **`AddDrinkFragment`** — форма коктейля: название, описание, фото, ингредиенты
  (с количеством и единицей), оценка (`RangeBar`), газированность/алкогольность, бокал,
  заметки. Реализует `AddImageDialogFragmentCallback`, `AddTastesDialogFragmentCallback`,
  `BackButtonListener`.
- **`AddDrinkIngredientsFragment`** — выбор ингредиентов с поиском и множественным выбором;
  синхронизируется с `AddDrinkFragment` через `SharedViewModel`.
- **`AddIngredientFragment`** — форма ингредиента: название, описание, заметки, фото.
  Удаление запрещено, если ингредиент используется в коктейлях.

### Диалоги

- **`AddImageDialogFragment`** — выбор источника фото (камера / галерея / удалить).
- **`AddTastesDialogFragment`** — множественный выбор вкусов (`R.array.taste_name`).

### Прочее

- **`AboutFragment`** — экран «О приложении» (ссылка на `ResourcesActivity`).
- **`SettingsFragment`** — выбор темы (`ThemeItemAdapter`).
- **`TabContainerFragment`** — контейнер вложенной навигации вкладки.

## Адаптеры (`ui/adapters`)

| Адаптер | Модель | Layout | Особенности |
|---------|--------|--------|-------------|
| `DrinkCardItemAdapter` | `ArrayList<DrinkEntity>` | `item_card_view_cocktail` | Карточки, Glide, `LoadMoreListener` (пагинация) |
| `LocalDrinksListAdapter` | `List<Drink>` | `item_simple_drink` | `INameableAdapter`, `DiffUtil` |
| `DrinkSimpleItemAdapter` | `List<Drink>` | `item_simple_drink` | Простые строки с кружком |
| `IngredientsItemAdapter` | `List<Ingredient>` | `item_simple_drink` | `INameableAdapter`, `DiffUtil` |
| `IngredientsListItemAdapter` | `List<Ingredient>` | `item_selectable_ingredient` | `Filterable`, множественный выбор |
| `AddDrinkIngredientItemAdapter` | `List<WholeCocktail>` | `add_drink_ingredient_item` | Поля количества + spinner единиц |
| `LocalDrinkIngredientItemAdapter` | `List<WholeCocktail>` | `item_single_drink_ingredient` | Ингредиенты локального напитка |
| `RemoteDrinkIngredientItemAdapter` | `List<WholeCocktail>` | `item_single_drink_ingredient` | Ингредиенты удалённого напитка |
| `ThemeItemAdapter` | `ArrayList<ThemeItem>` | `item_theme` | Цвет, галочка выбора, название |

## Темы

`ui/utils/ThemeUtils`:

| Константа | Значение | Стиль |
|-----------|----------|-------|
| `INDIGO` | 0 | `R.style.AppTheme_Indigo` |
| `PINK` | 1 | `R.style.AppTheme_Purple` |
| `BLACK` | 2 | `R.style.AppTheme_Black` |

- `onActivityCreateSetTheme(Activity)` — применяет тему при старте;
- `changeToTheme(Activity, int)` — сохраняет выбор в `getPreferences(MODE_PRIVATE)`
  (ключ `Constants.Extra.APP_THEME`) и перезапускает Activity с анимацией.

Базовый стиль — `AppTheme` (`Theme.AppCompat.Light.NoActionBar`), определён в
`res/values/styles.xml`.

## Ресурсы и локализация

- Строки и массивы — `res/values/` (русский) и `res/values-en/` (английский).
- Иконки/изображения — `res/drawable-*` (mdpi…xxxhdpi).
- Меню — `res/menu/` (`list_with_search_menu`, `add_drink_ingredient_menu`,
  `add_edit_item_menu`, `searchfragment_menu`).

## Нижняя навигация

`MainActivity` использует `AHBottomNavigation` (библиотека `com.aurelhubert`) с тремя
пунктами: «Поиск», «Коктейли», «Ингредиенты». Первый показ открывает вкладку «Коктейли»
(`setCurrentItem(1)`).
