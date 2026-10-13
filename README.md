# HomeBar

Android-приложение для поиска, создания и ведения коллекции рецептов коктейлей.

Приложение умеет искать напитки во внешней базе **Absolut Drinks**, сохранять понравившиеся
рецепты в локальную базу, а также создавать и редактировать собственные коктейли и ингредиенты.

## Возможности

- **Поиск коктейлей** — по названию и по расширенным условиям (тип бокала, вкус, ингредиент,
  крепость, цвет, газированность, алкогольность, навык приготовления).
- **Локальная коллекция** — сохранение найденных рецептов на устройство (работает офлайн).
- **Свои рецепты** — создание и редактирование коктейлей и ингредиентов, загрузка фото
  из галереи или камеры.
- **Ингредиенты** — база ингредиентов с описанием, заметками и связанными коктейлями.
- **Темы оформления** — три цветовые схемы (Indigo, Purple, Black).
- **Локализация** — русский и английский языки интерфейса.

## Технологии

| Компонент | Технология |
|-----------|------------|
| Язык | Java 17 |
| Архитектура | MVVM (ViewModel + LiveData/MediatorLiveData) |
| Локальная БД | Room 2.6.1 |
| Сеть | Retrofit 2.9.0 + OkHttp + Gson 2.10.1 |
| Навигация | Cicerone 2.1.0 |
| Изображения | Glide 4.16.0 |
| UI | AndroidX, Material Components, AHBottomNavigation, MaterialScrollBar, MaterialRangeBar, Spectrum |
| Сборка | Gradle 8.14.3 + Android Gradle Plugin 8.13.2 |

- `compileSdk` / `targetSdk`: **35**, `minSdk`: **21**
- `applicationId`: `com.sorokinuladzimir.homebarassistant`

## Быстрый старт

Требования: **JDK 17** и Android SDK (Platform 35, Build-Tools 35).

```bash
# сборка debug-APK
./gradlew :app:assembleDebug

# сборка и установка на подключённое устройство/эмулятор
./gradlew :app:installDebug

# запуск приложения
adb shell am start -n com.sorokinuladzimir.homebarassistant/.SplashActivity
```

Подробности по настройке окружения и решению проблем — в [docs/BUILD.md](docs/BUILD.md).

## Структура проекта

```
HomeBar/
├── app/
│   └── src/main/
│       ├── java/com/sorokinuladzimir/homebarassistant/
│       │   ├── BarApp.java              # Application, глобальные синглтоны
│       │   ├── MainActivity.java        # хост нижней навигации
│       │   ├── SplashActivity.java      # экран-заставка (launcher)
│       │   ├── BarDataRepository.java   # единый источник данных
│       │   ├── BarData.java             # контракт репозитория
│       │   ├── AppExecutors.java        # пулы потоков (disk/network/main)
│       │   ├── Constants.java           # ключи API, URL, Extra-ключи
│       │   ├── db/                      # Room: entity, dao, mapper, converter
│       │   ├── net/                     # Retrofit: api, entity, ServiceGenerator
│       │   ├── viewmodel/               # 10 ViewModel-классов
│       │   └── ui/                      # fragments, adapters, subnavigation, utils
│       └── res/                         # layout, drawable, values (+values-en)
├── docs/                                # документация
├── build.gradle                         # корневой скрипт (AGP 8.13.2)
├── settings.gradle                      # репозитории и модули
└── gradle.properties                    # AndroidX / Jetifier
```

## Документация

- [Архитектура](docs/ARCHITECTURE.md) — слои приложения, MVVM, навигация, репозиторий.
- [Сборка и окружение](docs/BUILD.md) — требования, команды, модернизация и troubleshooting.
- [База данных](docs/DATABASE.md) — схема Room, сущности, DAO, связи.
- [Сеть](docs/NETWORKING.md) — Absolut Drinks API, эндпоинты, модели, пагинация.
- [UI и навигация](docs/UI.md) — экраны, карта навигации, адаптеры, темы.

## Лицензия и источники данных

Данные о коктейлях предоставляются сервисом [Absolut Drinks](https://www.absolutdrinks.com).
Иконки — [Freepik](https://www.freepik.com/).
