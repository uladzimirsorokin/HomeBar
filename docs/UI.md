# UI и навигация

UI полностью на **Jetpack Compose (Material 3)**. Навигация — **Navigation Compose**,
единственный хост — `MainActivity` (`HomeBarApp()` → `NavHost`).

## Точки входа

| Компонент | Роль |
|-----------|------|
| `SplashActivity` | Launcher, сразу открывает `MainActivity` |
| `MainActivity` | `@AndroidEntryPoint`, Compose-хост: тема + `NavHost` + нижняя навигация |
| `ResourcesActivity` | Экран «Ресурсы» (ссылки на Freepik и TheCocktailDB) |

## Маршруты

Определены в `ui/navigation/Routes.kt`; граф — `homeBarGraph(...)` в `HomeBarNavHost.kt`.

| Route | Экран (`@Composable`) | ViewModel |
|-------|-----------------------|-----------|
| `found_drinks?conditions={conditions}` | `FoundDrinksScreen` | `FoundDrinksViewModel` |
| `search_filter` | `SearchDrinksScreen` | — (собирает `SearchConditions`) |
| `drinks_list` | `DrinksListScreen` | `DrinkListViewModel` |
| `ingredients_list` | `IngredientsListScreen` | `IngredientListViewModel` |
| `remote_drink/{drinkId}` | `DrinkDetailScreen` | `RemoteDrinkViewModel` |
| `local_drink/{extra_id}?editable={editable}` | `DrinkDetailScreen` | `DrinkViewModel` |
| `add_drink/{extra_id}` | `AddDrinkScreen` | `AddDrinkViewModel` |
| `add_drink_ingredients` | `SelectIngredientsScreen` | `AddDrinkIngredientsViewModel` |
| `local_ingredient/{extra_id}?editable={editable}` | `IngredientDetailScreen` | `IngredientViewModel` |
| `add_ingredient/{extra_id}` | `AddIngredientScreen` | `AddIngredientViewModel` |
| `about` | `AboutScreen` | — |
| `settings` | `SettingsScreen` | `ThemeViewModel` (activity-scope) |

Стартовый маршрут — `drinks_list`.

## Нижняя навигация

`HomeBarBottomBar` (Material 3 `NavigationBar`) с тремя вкладками:

| Индекс | Вкладка | Корневой маршрут | Иконка |
|--------|---------|------------------|--------|
| 0 | Поиск | `found_drinks` | `Icons.Filled.Search` |
| 1 | Коктейли | `drinks_list` | `R.drawable.cocktail` |
| 2 | Ингредиенты | `ingredients_list` | `R.drawable.bottles` |

Активная вкладка выводится из текущего route (`tabForRoute`). Переключение —
`navigateToTab` с `popUpTo(startDestination) { saveState = true }`.

## Экраны

### Списки и поиск

- **`FoundDrinksScreen`** — результаты удалённого поиска (`LazyColumn`), переход к деталям и к
  расширенному поиску (FAB `Search`). Пагинации нет — TheCocktailDB отдаёт весь результат сразу.
- **`SearchDrinksScreen`** — фильтр: название коктейля (текст), бокал, вкус, ингредиент
  (dropdown), газированность/алкогольность (switch), цвет (кружки), навык (`Slider`). Собирает
  `SearchConditions` и переходит к `found_drinks`. Сверху — блок **AI-поиска**: описание
  коктейля и кнопка «Найти с AI» (локальный LLM строит запрос, см. [AI.md](AI.md)).
  TheCocktailDB поддерживает только имя, ингредиент, бокал и алкогольность; остальные
  параметры учитываются AI как подсказки (см. [NETWORKING.md](NETWORKING.md)).
- **`DrinksListScreen`** — список локальных коктейлей с поиском и FAB создания.
- **`IngredientsListScreen`** — список локальных ингредиентов с поиском и FAB создания.

### Детали

- **`DrinkDetailScreen`** — детали коктейля с **collapsing-заголовком** (изображение схлопывается
  в тулбар с названием при скролле). Используется и для удалённого (`showNotes = false`, кнопка
  «Добавить» сохраняет рецепт), и для локального (`showNotes = true`, кнопка «Редактировать»).
- **`IngredientDetailScreen`** — деталь ингредиента с таким же collapsing-заголовком: описание,
  заметки и список связанных коктейлей.

Общий компонент заголовка — `CollapsingDetailScaffold` (`core/ui/components`): изображение,
градиентный скрим, кнопка «назад», заголовок и слот для FAB.

### Редактирование

- **`AddDrinkScreen`** — форма коктейля: название, описание, фото, ингредиенты (количество,
  единица), вкусы, оценка (`Slider`), газированность/алкогольность, бокал, заметки.
- **`SelectIngredientsScreen`** — выбор ингредиентов с поиском и множественным выбором;
  синхронизируется с формой напитка через `SharedViewModel`.
- **`AddIngredientScreen`** — форма ингредиента: название, описание, заметки, фото.

### Диалоги

- **`AddImageDialog`** — выбор источника фото (камера / галерея / удалить).
- **`AddTastesDialog`** — множественный выбор вкусов (`R.array.taste_name`).

### Прочее

- **`AboutScreen`** — «О приложении» (текст из `R.string.about_app_text`, ссылка на
  `ResourcesActivity`).
- **`SettingsScreen`** — выбор темы.
- **`ResourcesScreen`** — ссылки на источники данных.

## Общие компоненты (`core/ui/components`)

| Компонент | Назначение |
|-----------|------------|
| `EmptyState` | Заглушка «список пуст» (иконка + текст) |
| `DetailCard` | Карточка с заголовком и контентом |
| `SimpleListItem` | Строка списка: круглая картинка + название |
| `AddImageDialog` | Диалог выбора источника изображения |
| `AddTastesDialog` | Диалог выбора вкусов |
| `rememberImagePicker` | Обёртка над Activity Result API (камера/галерея) |
| `imageModel(String?)` | Построение модели для Coil (`http` URL или `File`) |
| `HomeBarTopBar` | Верхняя панель (в `ui/navigation`) |

## Темы

Три схемы в `core/ui/theme` (`ThemeVariant`): **Indigo (0)**, **Purple (1)**, **Black (2)**.

- выбор темы хранится в DataStore (`core/data/ThemePreferences`, ключ `app_theme`);
- `ThemeViewModel` отдаёт `StateFlow<Int>`;
- `MainActivity`/`ResourcesActivity` применяют тему через `HomeBarTheme(variant = ...)`.

## Ресурсы и локализация

- Строки и массивы — `res/values/` (русский) и `res/values-en/` (английский).
- Иконки/изображения — `res/drawable*` (png + vector), логотип — `res/mipmap-*`.
- Векторные иконки Material — из `material-icons-core`.
