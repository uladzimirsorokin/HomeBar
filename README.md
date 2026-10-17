# HomeBar

Android-приложение для поиска, создания и ведения коллекции рецептов коктейлей.

Приложение умеет искать напитки во внешней базе **TheCocktailDB**, сохранять понравившиеся
рецепты в локальную базу, а также создавать и редактировать собственные коктейли и ингредиенты.

## Возможности

- **Поиск коктейлей** — по названию, ингредиенту, бокалу и алкогольности (источник —
  TheCocktailDB). Фильтры «вкус/цвет/навык/газированность» пока сохраняются в запросе, но
  игнорируются источником.
- **AI-поиск** — опишите коктейль словами, локальный LLM (`gpt-oss:20b`) превратит
  описание в запрос к TheCocktailDB и учтёт выбранные фильтры.
- **Умная пост-обработка** — инструкции переводятся на язык приложения, а количество и
  единицы измерения разделяются по допустимым единицам.
- **Локальная коллекция** — сохранение найденных рецептов на устройство (работает офлайн).
- **Свои рецепты** — создание и редактирование коктейлей и ингредиентов, загрузка фото
  из галереи или камеры.
- **Ингредиенты** — база ингредиентов с описанием, заметками и связанными коктейлями.
- **Темы оформления** — три цветовые схемы (Indigo, Purple, Black), выбор сохраняется в DataStore.
- **Локализация** — русский и английский языки интерфейса.

## Технологии

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 2.2.0 |
| UI | Jetpack Compose (Material 3) |
| Архитектура | MVVM |
| Асинхронность | Coroutines + Flow / StateFlow |
| Навигация | Navigation Compose |
| DI | Hilt 2.57.1 |
| Локальная БД | Room 2.6.1 (Kotlin, Flow, KSP) |
| Сеть | Retrofit 2.9.0 + OkHttp 4.12 + kotlinx.serialization (TheCocktailDB) |
| AI | локальный OpenAI-совместимый сервер (Ollama, `gpt-oss:20b`) |
| Изображения | Coil 2.7.0 |
| Настройки | DataStore (Preferences) |
| Сборка | Gradle 8.14.3 + AGP 8.13.2, R8 |
| Тесты | JUnit + MockK + Coroutines Test + Compose UI Test |
| CI | GitHub Actions (build / test / lint) |

- `compileSdk` / `targetSdk`: **35**, `minSdk`: **21**
- `applicationId`: `com.sorokinuladzimir.homebarassistant`

## Быстрый старт

Требования: **JDK 17** и Android SDK (Platform 35, Build-Tools 35).

```bash
# сборка debug-APK
./gradlew :app:assembleDebug

# сборка release-APK (с R8)
./gradlew :app:assembleRelease

# unit-тесты
./gradlew :app:testDebugUnitTest

# instrumented-тесты (нужен эмулятор/устройство)
./gradlew :app:connectedDebugAndroidTest

# lint
./gradlew :app:lintDebug

# установка и запуск
./gradlew :app:installDebug
adb shell am start -n com.sorokinuladzimir.homebarassistant/.SplashActivity
```

API-ключ и URL TheCocktailDB можно переопределить в `local.properties`
(по умолчанию используется бесплатный тестовый ключ `1`):

```properties
COCKTAILDB_API_KEY=1
COCKTAILDB_ROOT=https://www.thecocktaildb.com/api/json/v1/
# локальный AI (OpenAI-совместимый сервер)
AI_BASE_URL=http://10.0.2.2:11434/v1/
AI_MODEL=gpt-oss:20b
AI_API_KEY=
```

Подробности по настройке окружения — в [docs/BUILD.md](docs/BUILD.md).

## Структура проекта

```
HomeBar/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/sorokinuladzimir/homebarassistant/
│       │   │   ├── BarApp.kt             # @HiltAndroidApp
│       │   │   ├── MainActivity.kt       # Compose-хост с NavHost
│       │   │   ├── SplashActivity.kt     # launcher
│       │   │   ├── ResourcesActivity.kt  # экран «Ресурсы»
│       │   │   ├── ai/                   # AI-клиент, промпты, планировщик, перевод
│       │   │   ├── BarData.kt            # контракт репозитория
│       │   │   ├── BarDataRepository.kt  # реализация (Room + Retrofit)
│       │   │   ├── Constants.kt          # FileProvider authority и константы
│       │   │   ├── core/
│       │   │   │   ├── data/             # DataStore (ThemePreferences)
│       │   │   │   ├── di/               # Hilt-модули (в т.ч. AiModule)
│       │   │   │   ├── ui/               # компоненты и тема Compose
│       │   │   │   └── util/             # MeasureParser и пр.
│       │   │   ├── db/                   # Room: entity, dao, mapper, converter
│       │   │   ├── net/                  # Retrofit: CocktailDbApi, dto, mapper, SearchConditions
│       │   │   ├── viewmodel/            # ViewModel-классы
│       │   │   └── ui/                   # feature-пакеты Compose-экранов + navigation
│       │   └── res/                      # drawable, mipmap, values, xml
│       ├── test/                         # unit-тесты
│       └── androidTest/                  # Compose UI-тесты
├── docs/                                 # документация
├── .github/workflows/                    # CI
├── build.gradle                          # корневой скрипт
├── settings.gradle                       # репозитории и модули
└── gradle/libs.versions.toml             # version catalog
```

## Тестирование и CI

- **Unit-тесты** (`src/test`): мапперы (`DrinkEntityToDrinkMapper`, `CocktailDbDrinkMapper`),
  `TasteConverter`, `TastesHelper`, `SearchConditions`, `AiSearchPlan`, `MeasureParser`,
  `SpinnerGlassMatcher`, `SharedViewModel`, `DrinkListViewModel`.
- **Compose UI-тесты** (`src/androidTest`): `EmptyState`, `DetailCard`, `AboutScreen`,
  `HomeBarBottomBar`.
- **CI** (`.github/workflows/ci.yml`): `assembleDebug` + `testDebugUnitTest` + `lintDebug`
  на каждый push/PR.

## Документация

- [Архитектура](docs/ARCHITECTURE.md) — слои, DI, репозиторий, навигация.
- [Сборка и окружение](docs/BUILD.md) — требования, команды, R8, troubleshooting.
- [База данных](docs/DATABASE.md) — схема Room, сущности, DAO, конвертеры.
- [Сеть](docs/NETWORKING.md) — TheCocktailDB API, эндпоинты, модели, условия поиска.
- [AI](docs/AI.md) — локальный LLM, AI-поиск, перевод, разбор единиц.
- [UI и навигация](docs/UI.md) — экраны, маршруты, темы, компоненты.
- [План рефакторинга](docs/REFACTORING_PLAN.md) — история перехода на Kotlin + Compose + Hilt.
- [ADR](docs/adr/) — ключевые архитектурные решения.
  - [0001. Kotlin + Compose + Hilt](docs/adr/0001-compose-modern-stack.md)
  - [0002. Смена источника данных на TheCocktailDB](docs/adr/0002-cocktaildb-provider.md)
  - [0003. Локальный AI для поиска и пост-обработки](docs/adr/0003-local-ai.md)

## Лицензия и источники данных

Данные о коктейлях предоставляются сервисом [TheCocktailDB](https://www.thecocktaildb.com).
Иконки — [Freepik](https://www.freepik.com/).
