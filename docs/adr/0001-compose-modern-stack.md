# ADR 0001. Переход на Kotlin + Jetpack Compose + Hilt

## Статус

Принято (этапы 0–8 рефакторинга завершены).

## Контекст

Исходное приложение (2018 год) было написано на **Java** с XML-разметкой, фрагментами,
навигацией на **Cicerone**, ручной проводкой зависимостей через статические синглтоны
`BarApp`, а также рядом устаревших сторонних UI-библиотек
(AHBottomNavigation, MaterialScrollBar, MaterialRangeBar, Spectrum, Glide, RxJava, Gson).
Проект был неработоспособен на современном окружении (jcenter/Bintray закрыты,
support-библиотеки, старый AGP).

## Решение

Поэтапно (strangler fig, «always-green») перевести приложение на современный стек:

| Слой | Было | Стало |
|------|------|--------|
| Язык | Java | Kotlin |
| Асинхронность | Executors + LiveData | Coroutines + Flow / StateFlow |
| UI | XML Views, Fragments | Jetpack Compose (Material 3) |
| Навигация | Cicerone | Navigation Compose |
| DI | синглтоны `BarApp` | Hilt |
| БД | Room (Java) | Room (Kotlin, Flow, KSP) |
| Сеть | Retrofit + Gson | Retrofit + OkHttp + kotlinx.serialization |
| Изображения | Glide | Coil |
| Настройки | SharedPreferences | DataStore (Preferences) |
| Тесты | нет | JUnit + MockK + Turbine + Compose UI Test |
| Сборка | Jetifier включён | Jetifier выключен, R8 |

## Обоснование ключевых решений

- **Compose + Navigation Compose** — декларативный UI и единый навигационный хост,
  убирает фрагменты, адаптеры и XML-разметку.
- **Hilt** — тестируемая DI вместо статических синглтонов; генерация кода через KSP.
- **kotlinx.serialization** — нативная для Kotlin сериализация; Gson удалён.
  Все поля `@Serializable`-классов получают явный `@SerialName`, чтобы **R8-обфускация
  не влияла на формат данных** (генерируемый сериализатор использует имена-константы).
- **DataStore** — асинхронные настройки; тема хранится как `StateFlow` и применяется без
  перезапуска Activity.
- **App-private storage для изображений** — `getExternalFilesDir(Pictures)` вместо публичного
  `Pictures`, что убирает `WRITE_EXTERNAL_STORAGE` и чинит работу при scoped storage (API 29+).
- **R8** — включён для release; keep-правила для сериализации, Retrofit, OkHttp, Room.

## Последствия

- В `app/src/main` не осталось Java-файлов; Jetifier и старые библиотеки удалены.
- ViewModel-слой по-прежнему отдаёт `LiveData` (мост `Flow → asLiveData()`), что сохранено
  для постепенности; при желании можно перевести на `StateFlow`.
- Схема Room (`version = 1`, `exportSchema = false`) не менялась; при изменениях нужны миграции.
- Данные о коктейлях по-прежнему берутся по `http://` из Absolut Drinks.
