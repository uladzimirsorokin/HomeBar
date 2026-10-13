# Сборка и окружение

## Требования

| Инструмент | Версия |
|-----------|--------|
| JDK | 17 (проект собирается на Java 17) |
| Gradle | 8.14.3 (через wrapper, скачивается автоматически) |
| Android Gradle Plugin | 8.13.2 |
| Android SDK Platform | 35 |
| Android Build-Tools | 35.0.0 |
| minSdk | 21 |
| compileSdk / targetSdk | 35 |

Путь к Android SDK задаётся в `local.properties` (файл не хранится в git):

```properties
sdk.dir=/path/to/Android/sdk
```

## Команды

```bash
# сборка debug-APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleDebug

# сборка release-APK
./gradlew :app:assembleRelease

# установка debug на устройство/эмулятор
./gradlew :app:installDebug

# очистка
./gradlew clean

# запуск после установки
adb shell am start -n com.sorokinuladzimir.homebarassistant/.SplashActivity
```

## Конфигурация Gradle

**`settings.gradle`** — репозитории и модули. Используется
`dependencyResolutionManagement` с `RepositoriesMode.PREFER_SETTINGS`:

```groovy
repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }                                  // Cicerone, MaterialScrollBar
    maven { url 'https://maven.aliyun.com/repository/public' }          // зеркало jcenter (MaterialRangeBar)
}
```

**`gradle.properties`** — флаги AndroidX и Jetifier:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
android.nonTransitiveRClass=false
android.nonFinalResIds=false
```

- `android.enableJetifier=true` — конвертирует сторонние библиотеки на `android.support.*`
  в AndroidX во время сборки.
- `android.nonFinalResIds=false` — сохраняет `R.id.*` константами (в коде есть `switch` по `R.id`).

## Зависимости

### AndroidX / Google

| Артефакт | Версия | Назначение |
|----------|--------|-----------|
| `androidx.appcompat:appcompat` | 1.7.0 | Базовые Activity/Fragment/темы |
| `com.google.android.material:material` | 1.12.0 | Material Components (FAB, AppBar и т.д.) |
| `androidx.recyclerview:recyclerview` | 1.3.2 | Списки |
| `androidx.cardview:cardview` | 1.0.0 | Карточки |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | Разметка |
| `androidx.swiperefreshlayout:swiperefreshlayout` | 1.1.0 | Pull-to-refresh |
| `androidx.lifecycle:lifecycle-extensions` | 2.2.0 | `ViewModelProviders`, `LiveData`, `MediatorLiveData` |
| `androidx.room:room-runtime` + `room-compiler` | 2.6.1 | Локальная БД |

### Сторонние

| Артефакт | Версия | Назначение |
|----------|--------|-----------|
| `com.github.terrakok:Cicerone` | 2.1.0 | Навигация |
| `com.github.bumptech.glide:glide` + `compiler` | 4.16.0 | Загрузка изображений |
| `com.google.code.gson:gson` | 2.10.1 | JSON |
| `com.squareup.retrofit2:retrofit` + `converter-gson` | 2.9.0 | HTTP-клиент |
| `com.aurelhubert:ahbottomnavigation` | 2.2.0 | Нижняя навигация |
| `com.github.turing-tech:MaterialScrollBar` | 12.5.1 | Алфавитный скроллбар |
| `com.appyvet:materialrangebar` | 1.4.6 | Выбор оценки (RangeBar) |
| `com.thebluealliance:spectrum` | 0.7.1 | Выбор цвета |
| `io.reactivex.rxjava2:rxjava` | 2.2.21 | RxJava (транзитивно) |

## История модернизации

Проект изначально был написан в 2018 году (Gradle 4.6, AGP 3.2.0-alpha16, `jcenter`,
Fabric/Crashlytics, support-библиотеки) и был неработоспособен на современном окружении.
Выполненные изменения:

1. **Toolchain** — Gradle wrapper → 8.14.3, AGP → 8.13.2, Java 17, добавлен `namespace`,
   `compileSdk`/`targetSdk` 35, `buildFeatures.buildConfig true`.
2. **AndroidX** — миграция всех импортов `android.support.*` и `android.arch.*` в `androidx.*`
   в Java-коде, разметке и меню; включены `android.useAndroidX` и `android.enableJetifier`.
3. **Зависимости** — обновлены до актуальных; Room 1.1.0 → 2.6.1, Glide 4.5 → 4.16 и т.д.
4. **Удалено** — Fabric/Crashlytics, Firebase, плагин OSS-licenses (инфраструктура jcenter/Bintray
   более недоступна).
5. **Проблемные артефакты:**
   - `com.tbruyelle.rxpermissions2:rxpermissions` удалён — его AAR содержал `lint.jar`,
     собранный под JDK 26, что ломало Jetifier. Разрешения переведены на
     `ContextCompat` / `ActivityCompat` / `Fragment.requestPermissions`.
   - `com.appyvet:materialrangebar` взят версии 1.4.6 через зеркало jcenter (Aliyun):
     версия 1.3 в Maven Central содержит другой пакет (`com.appyvet.rangebar`) и несовместимые атрибуты.

## Troubleshooting

**`Unsupported class file major version 70` в Jetifier**
Какой-то AAR содержит `lint.jar`, собранный новым JDK. Проверить артефакты можно скриптом,
сканирующим `.class`-версии внутри `.aar`/`.jar` в `~/.gradle/caches`. Решение — исключить
или заменить такой артефакт (как сделано с `rxpermissions`).

**`attribute mrb_* not found` при линковке ресурсов**
Установлена несовместимая версия MaterialRangeBar. Нужна 1.4.x (с префиксом `mrb_`),
а не 1.3.

**`cannot find symbol BuildConfig`**
Для AGP 8 генерация `BuildConfig` отключена по умолчанию. В `app/build.gradle` должно быть
`buildFeatures { buildConfig true }`.

**`constant expression required` в `switch (R.id...)`**
Включить `android.nonFinalResIds=false` в `gradle.properties`.

**Не резолвятся зависимости из jcenter**
jcenter/Bintray закрыты. Все артефакты перенесены на `mavenCentral`/`google`/JitPack;
для MaterialRangeBar используется зеркало `https://maven.aliyun.com/repository/public`.

**Запуск на Android 13+ и разрешения**
`WRITE_EXTERNAL_STORAGE` на API 33+ не выдаётся и не запрашивается; выбор фото из галереи
может не работать, приложение при этом запускается и функционирует.
