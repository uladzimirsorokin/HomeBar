# Сборка и окружение

## Требования

| Инструмент | Версия |
|-----------|--------|
| JDK | 17 |
| Gradle | 8.14.3 (через wrapper) |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.2.0 |
| Android SDK Platform | 35 |
| Android Build-Tools | 35.0.0 |
| minSdk | 21 |
| compileSdk / targetSdk | 35 |

Путь к Android SDK задаётся в `local.properties` (файл не хранится в git):

```properties
sdk.dir=/path/to/Android/sdk
# опционально — переопределение доступа к TheCocktailDB
COCKTAILDB_API_KEY=1
COCKTAILDB_ROOT=https://www.thecocktaildb.com/api/json/v1/
# опционально — локальный AI (OpenAI-совместимый сервер)
AI_BASE_URL=http://10.0.2.2:11434/v1/
AI_MODEL=gpt-oss:20b
AI_API_KEY=
```

## Команды

```bash
# сборка debug-APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleDebug

# сборка release-APK (R8: minify + shrinkResources)
./gradlew :app:assembleRelease

# unit-тесты (JVM)
./gradlew :app:testDebugUnitTest

# instrumented / Compose UI-тесты (нужен эмулятор или устройство)
./gradlew :app:connectedDebugAndroidTest

# lint
./gradlew :app:lintDebug

# установка debug на устройство/эмулятор
./gradlew :app:installDebug

# очистка
./gradlew clean

# запуск после установки
adb shell am start -n com.sorokinuladzimir.homebarassistant/.SplashActivity
```

## Конфигурация Gradle

**`settings.gradle`** — репозитории и модули (`PREFER_SETTINGS`):

```groovy
repositories {
    google()
    mavenCentral()
}
```

**`gradle.properties`**:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
kotlin.code.style=official
android.useAndroidX=true
ksp.useKSP2=false
```

- Jetifier выключен — все зависимости AndroidX-нативные.
- `nonTransitiveRClass` / `nonFinalResIds` не переопределяются (значения по умолчанию).
- `ksp.useKSP2=false` — совместимость с Room 2.6.1.

Версии зависимостей централизованы в `gradle/libs.versions.toml` (version catalog).

## Зависимости

### Compose

| Артефакт | Назначение |
|----------|-----------|
| `androidx.compose:compose-bom` | BOM версий Compose |
| `androidx.compose.material3:material3` | Material 3 |
| `androidx.activity:activity-compose` | `setContent`, Activity Result |
| `androidx.navigation:navigation-compose` | Навигация |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel()` |
| `androidx.lifecycle:lifecycle-viewmodel-compose` / `-runtime-compose` | ViewModel / `collectAsStateWithLifecycle` |
| `io.coil-kt:coil-compose` | Загрузка изображений |

### DI / Data / Network

| Артефакт | Назначение |
|----------|-----------|
| `com.google.dagger:hilt-android` + `hilt-compiler` (KSP) | DI |
| `androidx.room:room-runtime` / `room-ktx` / `room-compiler` (KSP) | Локальная БД (Flow) |
| `androidx.datastore:datastore-preferences` | Настройки (тема) |
| `com.squareup.retrofit2:retrofit` + `retrofit2-kotlinx-serialization-converter` | HTTP |
| `com.squareup.okhttp3:okhttp` | HTTP-клиент |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | JSON |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Корутины |

### Тесты

| Артефакт | Назначение |
|----------|-----------|
| `junit:junit` | unit-тесты |
| `io.mockk:mockk` | mock-объекты |
| `app.cash.turbine:turbine` | тестирование Flow |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `runTest`, `Dispatchers.setMain` |
| `androidx.arch.core:core-testing` | `InstantTaskExecutorRule` (LiveData) |
| `androidx.compose.ui:ui-test-junit4` / `ui-test-manifest` | Compose UI-тесты |
| `androidx.test.ext:junit` | instrumented runner |

## R8 / ProGuard

Release-сборка включает `minifyEnabled true` и `shrinkResources true`.
Правила — в `app/proguard-rules.pro`:

- keep-правила для `kotlinx.serialization` (сериализаторы и `Companion`);
- keep-правила для Retrofit (аннотированные HTTP-методы) и OkHttp;
- keep `RoomDatabase`-подклассов.

Все `@Serializable`-поля имеют явный `@SerialName`, поэтому обфускация не влияет на формат
данных.

Команды для проверки release-сборки:

```bash
./gradlew :app:assembleRelease
```

Mapping лежит в `app/build/outputs/mapping/release/`.

## CI

`.github/workflows/ci.yml` (на push/PR): JDK 17 + Android SDK + Gradle, затем
`assembleDebug`, `testDebugUnitTest`, `lintDebug`. Отчёты тестов и lint выгружаются артефактом.

## Troubleshooting

**`cannot find symbol BuildConfig`**
Для AGP 8 генерация `BuildConfig` отключена по умолчанию. В `app/build.gradle` должно быть
`buildFeatures { buildConfig true }`.

**Hilt: `[Hilt] Processing did not complete`**
Проверьте, что применяются плагины `ksp` и `hilt`, и что все `@HiltViewModel`/`@AndroidEntryPoint`
корректно аннотированы.

**Ошибки R8 по kotlinx.serialization**
Убедитесь, что на всех `@Serializable`-классах стоит `@SerialName` для полей, а в
`proguard-rules.pro` присутствуют keep-правила для `$$serializer`/`Companion`.

**Не запускаются instrumented-тесты**
Нужен подключённый эмулятор/устройство (`adb devices`). Compose UI-тесты требуют
`debugImplementation ui-test-manifest`.
