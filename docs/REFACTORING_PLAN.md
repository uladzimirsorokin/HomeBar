# План рефакторинга HomeBar на современный стек

## Цели

1. Перевести проект с Java/Views на **Kotlin + Jetpack Compose**.
2. Внедрить **Hilt** вместо статических синглтонов `BarApp`.
3. Перейти на **Coroutines/Flow**, **Compose Navigation**, **Coil**, **DataStore**.
4. Убрать легаси: Cicerone, support-библиотеки, `Jetifier`, устаревшие third-party библиотеки.
5. Сохранять работоспособность: **после каждого этапа проект собирается и запускается**.

## Принципы

- **Strangler Fig** — новый стек строится рядом со старым, экраны переносятся по одному.
- **Always-green** — критерий выхода любого этапа: `./gradlew :app:assembleDebug` проходит
  и приложение запускается на эмуляторе без крашей (smoke-тест).
- **Без «большого взрыва»** — на каждом этапе можно остановиться, оставив рабочее приложение.
- **Маленькие коммиты** — один логический шаг = один коммит с зелёной сборкой.

## Целевой стек

| Слой | Было | Станет |
|------|------|--------|
| Язык | Java | Kotlin |
| Асинхронность | Executors + LiveData | Coroutines + Flow / StateFlow |
| UI | XML Views, Fragments | Jetpack Compose (Material 3) |
| Навигация | Cicerone | Compose Navigation |
| DI | синглтоны `BarApp` | Hilt |
| Локальная БД | Room 2.6.1 (Java) | Room (Kotlin, Flow, миграции) |
| Сеть | Retrofit + Gson | Retrofit + OkHttp + kotlinx.serialization |
| Изображения | Glide | Coil (Compose) |
| Настройки | SharedPreferences | DataStore (Preferences) |
| Разрешения/камера | `requestPermissions`, `startActivityForResult` | Activity Result API |
| Сторонние UI-либы | AHBottomNavigation, MaterialScrollBar, MaterialRangeBar, Spectrum | компоненты Material 3 / кастомные Compose |
| Тесты | нет | JUnit + MockK + Turbine + Compose UI Test |
| Сборка | Jetifier включён | Jetifier выключен, AndroidX нативно |

## Целевая структура пакетов

```
com.sorokinuladzimir.homebarassistant
├── HomeBarApp.kt                 @HiltAndroidApp
├── MainActivity.kt               @AndroidEntryPoint, Compose-хост
├── core/
│   ├── di/                       Hilt-модули
│   ├── ui/theme/                 Compose-тема (цвета, типографика, темы Indigo/Purple/Black)
│   └── util/
├── data/
│   ├── local/                    Room: entity, dao, database, converter
│   ├── remote/                   Retrofit: api, dto, interceptor
│   ├── mapper/
│   └── repository/               реализации
├── domain/
│   ├── model/                    доменные модели
│   ├── repository/               интерфейсы
│   └── usecase/                  use case'ы (по мере необходимости)
└── ui/                           feature-based
    ├── search/  drinks/  ingredients/
    ├── adddrink/  addingredient/
    ├── about/  settings/
    └── navigation/
```

## Карта этапов

| # | Этап | Итог | Риск |
|---|------|------|------|
| 0 | Подготовка и фундамент | Kotlin, Compose, Hilt подключены; пакет `core` | низкий |
| 1 | Data-слой на Kotlin + Flow | Room/Repository на Kotlin, Flow→LiveData мост | средний |
| 2 | Hilt | Замена синглтонов `BarApp` на DI | средний |
| 3 | Сеть | Retrofit + kotlinx.serialization, `Result` | низкий |
| 4 | Compose-фундамент | Тема, Compose-хост, интероп с фрагментами | средний |
| 5 | Поэкранная миграция UI | Все экраны на Compose (в оболочке Fragment) | высокий |
| 6 | Compose Navigation | Замена Cicerone, удаление фрагментов-оболочек | высокий |
| 7 | Удаление легаси | Выключен Jetifier, убраны старые либы | средний |
| 8 | Качество | Тесты, DataStore, Activity Result, CI, R8 | низкий |

---

## Этап 0. Подготовка и фундамент

**Цель:** подготовить проект к Kotlin/Compose/Hilt, не меняя функциональность.

Задачи:
- [ ] Подключить Kotlin: плагин `org.jetbrains.kotlin.android`, `kotlinOptions.jvmTarget = "17"`.
- [ ] Подключить Compose: `buildFeatures.compose = true`, `composeOptions`, BOM
      (`androidx.compose:compose-bom`), `material3`, `ui-tooling`, `activity-compose`.
- [ ] Добавить Compose-компилятор (для Kotlin 2.x — плагин `kotlin("plugin.compose")`).
- [ ] Добавить Hilt: плагин `com.google.dagger.hilt.android`, `ksp` (или kapt) + `hilt-android`.
- [ ] Включить KSP для Room/Glide-заменителей (по мере надобности).
- [ ] Создать пакет `core/ui/theme` с базовой Compose-темой (перенос цветов из `colors.xml`).
- [ ] Настроить `kotlin.code.style=official`, форматирование (ktlint/spotless — опционально).
- [ ] Зафиксировать версии через version catalog `gradle/libs.versions.toml`.

**Артефакты:** рабочий проект с Kotlin+Compose+Hilt на борту, старая UI работает.
**Критерий выхода:** `assembleDebug` зелёный, приложение запускается (UI прежний).

---

## Этап 1. Data-слой на Kotlin + Coroutines/Flow

**Цель:** перевести хранение и репозиторий на Kotlin с `Flow`, не меняя UI.

Задачи:
- [ ] Перевести `db/` на Kotlin: `Drink`, `Ingredient`, `DrinkIngredientJoin`, `Glass`,
      `Taste`, `WholeCocktail`, `TasteConverter`, DAO, `CocktailsDatabase`.
- [ ] DAO: возвращать `Flow<...>` вместо `LiveData<...>`; `suspend` для записи.
- [ ] Ввести доменные модели в `domain/model` и мапперы (Room-entity ⇄ domain).
- [ ] Интерфейсы репозиториев в `domain/repository`, реализации в `data/repository`.
- [ ] `AppExecutors` удалить; использовать `Dispatchers.IO` (в `core/util`).
- [ ] Мост `Flow → LiveData` (`asLiveData()`) для старых ViewModel, чтобы UI не менялся.
- [ ] Добавить `@Transaction`/миграции, `exportSchema = true`, папка `schemas/`.

**Критерий выхода:** старый UI работает, данные читаются/пишутся через новый репозиторий.

---

## Этап 2. Hilt

**Цель:** заменить ручные синглтоны на DI.

Задачи:
- [ ] `HomeBarApp` → `@HiltAndroidApp`.
- [ ] Модули: `DatabaseModule` (Room), `NetworkModule` (Retrofit/OkHttp), `RepositoryModule`.
- [ ] `@AndroidEntryPoint` на Activity/Fragment по мере миграции.
- [ ] ViewModel'и: `@HiltViewModel`, инъекция репозиториев; убрать кастомные `Factory`.
- [ ] Убрать статические аксессоры `BarApp.getBarRepository()/getDatabase()`.

**Критерий выхода:** приложение работает без статических синглтонов данных.

---

## Этап 3. Сеть

**Цель:** современный сетевой слой.

Задачи:
- [ ] DTO в `data/remote/dto` на Kotlin (`@Serializable`).
- [ ] Заменить Gson-конвертер на `kotlinx-serialization` (Retrofit converter).
- [ ] Интерцепторы (`apiKey`, `lang`, connectivity) перевести на Kotlin; заменить
      `getActiveNetworkInfo()` на `NetworkCapabilities`.
- [ ] Обёртка результата (`Result`/`ApiResult`) вместо исключений в UI.
- [ ] Вынести `apiKey`/base URL из `Constants` в `BuildConfig`/`local.properties`.
- [ ] Использовать `suspend`-функции Retrofit.

**Критерий выхода:** поиск коктейлей и детали работают через новый сетевой слой.

---

## Этап 4. Compose-фундамент

**Цель:** запустить Compose параллельно с фрагментами.

Задачи:
- [ ] Compose-тема (Material 3): перенос 3 тем (Indigo/Purple/Black), типографики, цветов.
- [ ] Общий `Scaffold`, `TopAppBar`, навигационные паттерны.
- [ ] Интероп-стратегия: новые Compose-экраны внутри фрагментов через `ComposeView`
      (навигация Cicerone пока сохраняется).
- [ ] Общий компонент списка/пустого состояния/загрузки (вместо повторяющихся ImageView).
- [ ] Подключить Coil для изображений в Compose.

**Критерий выхода:** хотя бы один экран (например, `AboutFragment`) переведён на Compose
через `ComposeView` и работает в старой навигации.

---

## Этап 5. Поэкранная миграция UI на Compose

Порядок — от простого к сложному; каждый экран = отдельный подэтап с зелёной сборкой.

- [ ] **5.1** `AboutFragment`, `SettingsFragment` (статика, выбор темы).
- [ ] **5.2** Списки: `DrinksListFragment`, `IngredientsListFragment` (LazyColumn + поиск).
      Замена `MaterialScrollBar` на Compose-решение (fast scroller) или отказ от него.
- [ ] **5.3** Детали: `LocalDrinkFragment`, `IngredientFragment`, `RemoteDrinkFragment`.
- [ ] **5.4** Вкладка «Поиск»: `FoundDrinksFragment`, `SearchDrinksFragment`
      (фильтры — Material 3, `Slider` вместо `RangeBar`, кастомный выбор цвета вместо Spectrum).
- [ ] **5.5** Формы: `AddDrinkFragment`, `AddIngredientFragment`, `AddDrinkIngredientsFragment`.
- [ ] **5.6** Диалоги: `AddImageDialogFragment`, `AddTastesDialogFragment` → Compose-диалоги.
- [ ] **5.7** Нижняя навигация: `AHBottomNavigation` → Material 3 `NavigationBar`.

**Критерий выхода:** все экраны рендерятся Compose (пока в оболочках Fragment).

---

## Этап 6. Compose Navigation вместо Cicerone

**Цель:** единый Compose-хост, отказ от Cicerone и фрагментов.

Задачи:
- [ ] Ввести `NavHost` с маршрутами, соответствующими текущим `Screens`.
- [ ] Перенести навигацию вкладок и вложенных экранов в Compose Navigation.
- [ ] Перевести экраны из «Compose внутри Fragment» в чистые `@Composable`.
- [ ] Удалить: `TabContainerFragment`, `MainActivity`-навигатор, `Screens`, `ui/subnavigation`,
      `BarApp.getRouter()/getNavigatorHolder()`.
- [ ] Общий ViewModel-scope для экранов-шагов (замена `SharedViewModel`).

**Критерий выхода:** навигация полностью на Compose Navigation, Cicerone удалён.

---

## Этап 7. Удаление легаси

Задачи:
- [ ] Удалить Java-фрагменты, адаптеры, XML-разметки, `values/styles.xml` (View-темы).
- [ ] Удалить старые библиотеки: AHBottomNavigation, MaterialScrollBar, MaterialRangeBar,
      Spectrum, Cicerone, Glide, Gson, RxJava.
- [ ] Выключить `android.enableJetifier=false`; убедиться, что все зависимости AndroidX-нативные.
- [ ] Убрать `android.nonTransitiveRClass=false` и `android.nonFinalResIds=false`
      (после отказа от `switch (R.id...)`).
- [ ] Убрать `local.properties`-зависимые хаки, если появятся.

**Критерий выхода:** сборка без Jetifier, минимальный набор зависимостей.

---

## Этап 8. Качество и инфраструктура

Задачи:
- [ ] Unit-тесты: репозитории, мапперы, ViewModel (MockK + Turbine + coroutines-test).
- [ ] Compose UI-тесты ключевых экранов.
- [ ] `DataStore` для темы и прочих настроек (вместо SharedPreferences).
- [ ] Activity Result API для камеры/галереи/разрешений (вместо legacy).
- [ ] R8/ProGuard: правила, `minifyEnabled true` для release.
- [ ] CI (GitHub Actions): сборка + тесты + lint на PR.
- [ ] Обновить `README`/`docs` под новый стек; добавить ADR.

**Критерий выхода:** зелёные тесты и CI, release собирается с R8.

---

## Замена сторонних библиотек

| Было | Станет |
|------|--------|
| `com.aurelhubert:ahbottomnavigation` | Material 3 `NavigationBar` |
| `com.github.turing-tech:MaterialScrollBar` | Compose fast-scroller / отказ |
| `com.appyvet:materialrangebar` | Material 3 `Slider` |
| `com.thebluealliance:spectrum` | Кастомный Compose color picker |
| `com.github.bumptech.glide:glide` | `io.coil-kt:coil-compose` |
| `ru.terrakok.cicerone` | `androidx.navigation:navigation-compose` |
| `com.google.code.gson:gson` | `kotlinx-serialization-json` |
| `io.reactivex.rxjava2:rxjava` | Coroutines/Flow |

## Риски и меры

| Риск | Мера |
|------|------|
| Долгая нерабочая сборка | Строгий принцип always-green, миграция по одному экрану |
| Смешивание Compose и View-навигации | Интероп через `ComposeView` до Этапа 6 |
| Сложные экраны (формы, камера) | Переносить в конце, после простых экранов |
| Потеря поведения/фич | Smoke-тест на эмуляторе после каждого этапа + ручной чек-лист |
| Расхождение схемы Room | `exportSchema=true`, явные миграции |
| Отсутствие тестов на старте | Ввести тесты с Этапа 1 на новые классы |

## Definition of Done (для каждого этапа)

1. `./gradlew :app:assembleDebug` — успешно.
2. Приложение ставится и запускается на эмуляторе, критические сценарии работают.
3. Нет новых ошибок в `logcat` (FATAL/AndroidRuntime).
4. Документация/комментарии обновлены, если менялось поведение.
5. Изменения зафиксированы отдельным коммитом.

## Журнал прогресса

| Этап | Статус | Заметки |
|------|--------|---------|
| 0. Фундамент | ✅ готово | Kotlin 2.2.0, Compose BOM 2025.06.01, Hilt 2.57.1, KSP 2.2.0-2.0.2; version catalog; тема `core/ui/theme` |
| 1. Data + Flow | ✅ готово | `db/` и `BarDataRepository`/`BarData` на Kotlin; DAO → `Flow`/`suspend`; мост `asLiveData()`; Room compiler переведён на KSP. KSP2 отключён (`ksp.useKSP2=false`) из-за несовместимости Room 2.6.1; `AppExecutors` оставлен для адаптеров (удалим на этапе 5) |
| 2. Hilt | ✅ готово | `@HiltAndroidApp`, модули `DatabaseModule`/`NetworkModule`/`AppModule`/`RepositoryModule`; `BarData`/`CocktailsDatabase`/`AbsolutDrinksApi`/`ImageHandler` через DI; 10 ViewModel → Kotlin `@HiltViewModel` (+`SavedStateHandle` для аргументов); фрагменты/`MainActivity` `@AndroidEntryPoint`. Убраны `BarApp.getDatabase()/getBarRepository()` и `ServiceGenerator`. `AppExecutors` оставлен статически для адаптеров (до этапа 5) |
| 3. Сеть | ✅ готово | DTO на Kotlin `@Serializable` (kotlinx.serialization) + `TasteDto`; Retrofit API → `suspend`; `NetworkModule` на kotlinx-конвертере; OkHttp 4.12; `NetworkUtil` → `NetworkCapabilities`; `ApiResult` + тосты в main-поток; ключ/URL → `BuildConfig`/`local.properties`. Gson оставлен только для Room-конвертера `TasteConverter` |
| 4. Compose-фундамент | ✅ готово | Тема `core/ui/theme` (+ `homeBarThemeVariant()` из prefs); общий компонент `EmptyState`; Coil подключён; интероп-стратегия: `ComposeView` в контентной области фрагмента (XML-toolbar сохраняется). Пилот — `AboutFragment`/`AboutScreen` на Compose |
| 5. Поэкранная миграция | ✅ готово | 5.1 Settings; 5.2 списки; 5.3 детали; 5.4 поиск; 5.5 формы; 5.6 диалоги; 5.7 нижняя навигация (`MainActivity` → Kotlin + Material 3 `NavigationBar`). Все экраны на Compose (в оболочках Fragment) |
| 6. Compose Navigation | ⬜ не начат | |
| 7. Удаление легаси | ⬜ не начат | |
| 8. Качество | ⬜ не начат | |
