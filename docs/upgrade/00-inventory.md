# Phase 0 — Inventory

**App:** System File Explorer (`com.syspolrer.systemfileexplorer`)  
**Ngày quét:** 2026-09-18  
**HEAD:** `d8751ef` (`initial code`) trên branch `main`  
**Phạm vi:** chỉ đọc. Không sửa code. Không commit.

Mọi version “latest” dưới đây đã verify trên Maven (Google / Maven Central) và/hoặc docs chính thức cùng ngày. Link kèm theo từng dòng.

---

## 1. Cấu trúc project

| Hạng mục | Hiện trạng |
|---|---|
| Số module | **1** — `:app` (`application`) |
| Loại module | App module duy nhất. Không có library / feature / dynamic-feature |
| KMP | Không |
| Compose | Không. UI = XML layout + Data Binding + Java/Kotlin Views |
| Version catalog `libs.versions.toml` | **Không có** |
| `buildSrc` / convention plugin | **Không có** |
| DSL | **Groovy** (`build.gradle`, `settings.gradle`). Không có `.kts` |
| Repositories | `google()` + `mavenCentral()` + `gradlePluginPortal()`. `FAIL_ON_PROJECT_REPOS`. **Không** `jcenter()` |
| CI | **Không** (không có `.github/workflows`) |
| Signing config / flavors | Không. `minifyEnabled false` cho cả debug và release |
| NDK / CMake / `jniLibs` nguồn | Không có source native. **Có** `.so` transitive từ `android-gif-drawable` (đã thấy trong `app/build/intermediates`) |
| Plugin hiện tại | `com.android.application`, `kotlin-android`, `kotlin-parcelize`, `kotlin-kapt` |

**Source (app/src/main):**

- Java: **70** file
- Kotlin: **12** file
- Layout XML: **54**
- Activities: 3 (`SplashActivity`, `MainActivity`, `SelectFileActivity`)
- Fragments: **30** (file manager: Images / Audio / Videos / Zips / Apps / Document / Download / More / Search / Favourites / Storage / Settings / …)
- Tests: 1 unit boilerplate + 1 instrumented boilerplate

**Kiến trúc runtime (rút gọn):**

- `MyApplication` — AdMob init + App Open Ad + `ProcessLifecycleOwner`
- Splash → xin `MANAGE_EXTERNAL_STORAGE` (API 30+) hoặc READ/WRITE (API < 30) → Main
- Main: Fragment navigation thủ công (`FragmentManager.replace`), banner AdMob dưới cùng
- File I/O: `java.io.File` + `Environment` + Apache Commons IO `FileUtils`
- Ảnh thumbnail: Glide (Picasso khai báo nhưng **không dùng**)
- DB: SQLite `DbHelper` (bảng favourite), không Room
- Ads: banner + interstitial + app open

---

## 2. Toolchain hiện tại

| Thành phần | Hiện tại | Latest stable đã verify (2026-09-18) | Ghi chú |
|---|---|---|---|
| JDK trên máy | Temurin **17.0.19** | JDK 17 vẫn là min của AGP 9.4 | Không pin `toolchain` trong Gradle |
| `jvmTarget` / `sourceCompatibility` | **1.8** | AGP 9 khuyến nghị khớp JDK; sẽ nâng (không đụng UI) | |
| Gradle wrapper | **8.2** (bin) | **9.7.1** (2026-08-19) | AGP 9.4.0 **min/default = Gradle 9.6.0**. Kotlin 2.4.20 test chính thức tới Gradle 9.7.0 |
| AGP | **8.2.2** | **9.4.0** (2026-09-01). Google Maven `latest` đang là `9.5.0-alpha06` → **không dùng alpha** | AGP 9.4 max API = **37**. AGP 9.0 là major breaking (built-in Kotlin, DSL mới, Variant API cũ bỏ) |
| Kotlin | **1.9.10** | **2.4.20** (2026-09-07) | Maven `org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20` tồn tại |
| KSP | **Không dùng** (có `kotlin-kapt`) | **2.3.12** (Maven, ~2026-09-17). GitHub tag public gần nhất thấy **2.3.11** (2026-08-03) | KSP 2.3.x đã tách version khỏi Kotlin. AGP 9 skill: KSP ≥ 2.3.6. KSP 2.3.12 min AGP 8.12.0 |
| kapt | **Bật** | kapt maintenance mode | Dùng sai artifact Glide (xem §4) |
| compileSdk | **34** | **37** (Android 17) | Docs: [Set up Android 17 SDK](https://developer.android.com/about/versions/17/setup-sdk). Bảng min tool: API 37 cần AGP ≥ **9.1.1** |
| targetSdk | **34** | Mục tiêu **37**. Play đang yêu cầu target ≥ **36** từ 2026-08-31 | Rollback hợp lệ: `targetSdk=36` + `compileSdk=37` |
| minSdk | **24** | Giữ nguyên (chưa hỏi) | Material 1.14 / Ads 24+ yêu cầu min 23 — không ép ta nâng |
| buildToolsVersion | Không khai | AGP 9.4 default **36.0.0** | Không cần khai tay |
| NDK | Không | AGP 9.4 default 28.2.13676358 | Chỉ liên quan nếu giữ gif-drawable `.so` |

**Docs toolchain:**

- AGP 9.4.0: https://developer.android.com/build/releases/agp-9-4-0-release-notes
- AGP compatibility / API 37 min AGP 9.1.1: https://developer.android.com/build/releases/about-agp
- AGP 9 built-in Kotlin: https://developer.android.com/build/migrate-to-built-in-kotlin
- AGP 9 migration skill: https://developer.android.com/agents/skills/build/agp/agp-9-upgrade/skill
- Gradle 9.7.1: https://docs.gradle.org/9.7.1/release-notes.html
- Kotlin 2.4.20: https://kotlinlang.org/docs/whatsnew2420.html
- Android 17 behavior (target 37): https://developer.android.com/about/versions/17/behavior-changes-17
- Play target API: target ≥ 36 từ 31/08/2026; API 37 bắt buộc ~08/2027

**Cờ Gradle hiện tại (`gradle.properties`):**

```
android.useAndroidX=true
android.nonTransitiveRClass=true
android.nonFinalResIds=false
android.enableJetifier=true          ← CỜ ĐỎ
org.gradle.jvmargs=-Xmx2048m
```

Không có `android.builtInKotlin`, `android.newDsl`, configuration cache.

---

## 3. Bảng dependency đầy đủ

Cột “Latest” = version **stable** verify trên Maven ngày 2026-09-18.  
Δ major: `=` cùng major, `+1` nhảy 1 major, `abandoned` không còn maintain.

### 3.1 Build plugins

| group:artifact | Hiện tại | Latest | Δ | Maintain? | Breaking? | Release notes |
|---|---|---|---|---|---|---|
| `com.android.tools.build:gradle` | 8.2.2 | **9.4.0** (2026-09-01) | +1 | Có | **Có — AGP 9 major** (built-in Kotlin, `kotlin-android` bỏ, `kotlinOptions` → `compilerOptions`, Variant API cũ bỏ, `lintOptions` → `lint`) | [AGP 9.4.0](https://developer.android.com/build/releases/agp-9-4-0-release-notes) · [built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin) |
| `org.jetbrains.kotlin:kotlin-gradle-plugin` | 1.9.10 | **2.4.20** (2026-09-07) | +1 | Có | Có (K2 default từ 2.0; không còn `composeOptions.kotlinCompilerExtensionVersion` — app **không dùng Compose**) | [whatsnew2420](https://kotlinlang.org/docs/whatsnew2420.html) |
| `org.jetbrains.kotlin.plugin.compose` | — | 2.4.20 | n/a | Có | n/a | **Không áp dụng** — app không Compose |
| `com.google.devtools.ksp` | — | **2.3.12** (Maven 2026-09-17) | mới | Có | Cần nếu migrate kapt→KSP | [ksp releases](https://github.com/google/ksp/releases) |
| Hilt / Firebase / protobuf / safeargs / spotless | — | — | — | — | — | Không có |

### 3.2 AndroidX / Google (runtime)

| group:artifact | Hiện tại | Latest | Δ | Maintain? | Breaking / UI risk | Docs |
|---|---|---|---|---|---|---|
| `androidx.appcompat:appcompat` | 1.6.1 | **1.8.0** (2026-08-12) | = | Có | Nhỏ. 1.8 kéo Activity 1.8 | [appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat) |
| `com.google.android.material:material` | 1.12.0 | **1.14.0** (2026-05-13) | = | Views **maintenance mode** (I/O 2026, Compose-first). 1.14 = stable Views cuối | **RỦI RO UI.** 1.14 thêm Material 3 Expressive. Theme hiện tại `Theme.Material3.Light.NoActionBar` — Expressive **opt-in** qua `Theme.Material3Expressive.*`, nhưng vẫn phải screenshot. minSdk lib = 23 | [1.14.0](https://github.com/material-components/material-components-android/releases/tag/1.14.0) · [getting-started](https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md) |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | **2.2.2** (2026-07-29) | = | Có | Thấp | [constraintlayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout) |
| `androidx.databinding` (via `buildFeatures.dataBinding true`) | AGP 8.2 | AGP 9.4 vẫn hỗ trợ | = | Có | Không đổi hierarchy | AGP 9 docs |
| `com.google.android.gms:play-services-ads` | 23.1.0 | Maven **25.5.0** (2026-09-17). Docs chính thức vẫn ghi **25.4.0** (2026-06-17) | +2 | Có | **Có.** v24 (2025-02-24): xóa `AppOpenAd.load(..., orientation, ...)`, xóa `APP_OPEN_AD_ORIENTATION_*`, minSdk 23, optimize init default on. v25 (2026-02-17): deprecation banner size cũ. Code app **đang gọi overload orientation** trong `MyApplication.kt` | [SDK](https://developers.google.com/admob/android/sdk) · [rel-notes](https://developers.google.com/admob/android/rel-notes) · [migration](https://developers.google.com/admob/android/migration) |
| `androidx.lifecycle` (`ProcessLifecycleOwner`, `@OnLifecycleEvent`) | transitive | latest lifecycle stable (xem androidx versions) | = | Có | `@OnLifecycleEvent` **deprecated** — `MyApplication` phải chuyển `DefaultLifecycleObserver` (không đổi UI) | [OnLifecycleEvent](https://developer.android.com/reference/kotlin/androidx/lifecycle/OnLifecycleEvent) |
| `androidx.loader` (`LoaderManager` / `AsyncTaskLoader`) | transitive qua appcompat? dùng trực tiếp | 1.2.0 (2026-07) | = | Maintenance | Loader API cũ nhưng **chưa removed**. Dùng ở `MainActivity`, `AppsInstalledFragment`, `AppListLoader` | |

**Không có BOM** (Compose/Firebase/OkHttp). App không khai `core-ktx`, `fragment-ktx`, `lifecycle-runtime-ktx`, `recyclerview` — chúng đi transitive.

### 3.3 Third-party

| group:artifact | Hiện tại | Latest | Δ | Maintain? | Breaking / UI | Docs |
|---|---|---|---|---|---|---|
| `org.apache.commons:commons-io` | **1.3.2** (2007-07-02) | Artifact này **đóng băng 1.3.2**. Artifact đúng: `commons-io:commons-io` **2.22.0** (2026-04-19) | +1 (và đổi GAV) | Có (artifact mới) | API `FileUtils` vẫn cùng package. Dùng `deleteDirectory` / `copyDirectoryToDirectory` / `copyFileToDirectory(..., true)` tại 7 fragment | [Maven](https://repo1.maven.org/maven2/commons-io/commons-io/maven-metadata.xml) · [changes](https://commons.apache.org/proper/commons-io/changes.html) |
| `com.github.bumptech.glide:glide` | 4.14.2 | **5.0.9** (2026-07-11) | +1 | Có | Major. App **không** dùng `GlideApp` / `@GlideModule` → processor có thể bỏ. `recyclerview-integration` đang `transitive = false` | [GitHub 5.0.9](https://github.com/bumptech/glide/releases/tag/v.5.0.9) · Maven latest 5.0.9 |
| `com.github.bumptech.glide:ksp` | 4.14.2 **qua `kapt`** | 5.0.9 | +1 | Có | **Cấu hình sai:** `kapt '…:ksp:4.14.2'` (plugin kapt + artifact KSP). Không có `@GlideModule` nên processor gần như vô dụng | [Glide setup](https://bumptech.github.io/glide/doc/download-setup.html) |
| `com.github.bumptech.glide:recyclerview-integration` | 4.14.2 | 5.0.9 | +1 | Có | Dùng `RecyclerViewPreloader` trong `AppsInstalledFragment` | |
| `com.google.code.gson:gson` | 2.11.0 | **2.14.0** (2026-04-23) | = | Có (Google **không khuyến nghị Gson trên Android** vì reflection + R8; minify đang tắt nên chưa đau) | Dùng `StorageUtils.kt` serialize `List<AppDataParcelable>` | [gson README](https://github.com/google/gson) |
| `com.squareup.picasso:picasso` | 2.8 | 2.8 / metadata `2.71828` (last 2020-08-10) | = | **Deprecated, không còn public Maven release** | **Không có import Picasso trong source** → dead dependency | [square/picasso](https://github.com/square/picasso) |
| `pl.droidsonroids.gif:android-gif-drawable` | 1.2.23 | **1.2.32** | = | Còn release | **Không có usage trong source.** Kéo native `libpl_droidsonroids_gif.so` (armeabi-v7a/arm64/x86/x86_64) → dính **16KB page size (API 36)** nếu giữ | [GitHub](https://github.com/koral--/android-gif-drawable/) |
| `com.tuyenmonkey:mkloader` | 1.4.0 | **1.4.0** (last Maven **2017-08-01**) | abandoned | **Không** | Dùng rộng: ~14 layout XML + ~11 adapter. **Thay = đổi UI (R1).** Phải pin | [nntuyen/mkloader](https://github.com/nntuyen/mkloader) |
| `com.jaredrummler:android-processes` | 1.0.3 | 1.1.1 (2017-09-09) | abandoned | **Không. Author: “No longer maintained. Ineffective on Android 7+.”** | **Không có usage trong source.** minSdk 24 = library đã chết trên mọi device của ta | [AndroidProcesses](https://github.com/jaredrummler/AndroidProcesses/) |
| `com.afollestad.material-dialogs:core` | **0.9.6.0** | Line 0.9.x chết. Line Kotlin **3.3.0** (last Maven **2021-02-21**) | abandoned / +3 | **Không** (cả 0.9 lẫn 3.x ngừng ~2021) | Dùng `MaterialDialog.Builder` (API Java cũ) trong `ShareTask.java` / `ShareAdapter.java`. 3.x rewrite Kotlin, **visual khác** → R1. Pin 0.9.6.0 trừ khi bạn duyệt thay | Maven metadata |

### 3.4 Test

| group:artifact | Hiện tại | Latest | Δ | Notes |
|---|---|---|---|---|
| `junit:junit` | 4.13.2 | **4.13.2** (last 4.x) | = | JUnit 5 là major khác — **không nhảy** trừ khi bạn duyệt. 1 test `2+2=4` |
| `androidx.test.ext:junit` | 1.1.5 | **1.3.0** (2025-07-30) | = | 1 test package name |
| `androidx.test.espresso:espresso-core` | 3.5.1 | **3.7.0** (2025-07-30) | = | [androidx.test](https://developer.android.com/jetpack/androidx/releases/test) |

### 3.5 Annotation processor

| Processor | Hiện tại | Ghi chú |
|---|---|---|
| `kotlin-kapt` | bật | Chỉ “dùng” cho Glide, và dùng **sai artifact** |
| Glide KSP/kapt | `kapt …:ksp:4.14.2` | Không có `@GlideModule` / `GlideApp` → có thể **gỡ processor** |
| Data Binding compiler | AGP built-in | Giữ. Không migrate ViewBinding (đổi binding API, không đổi XML nhưng vẫn đụng nhiều file — hỏi trước nếu muốn) |
| kapt → KSP | chưa | **Quyết định kiến trúc (R4).** Glide hỗ trợ KSP. Data Binding không cần KSP. Nếu gỡ Glide processor thì **có thể bỏ hẳn kapt** |

---

## 4. Cờ đỏ

| # | Cờ | Mức | Chi tiết |
|---|---|---|---|
| 1 | `android.enableJetifier=true` | Cao | Gần chắc vì `mkloader` 1.4.0 + `material-dialogs` 0.9.6.0 (support library). Bỏ jetifier chỉ được sau khi chứng minh không còn `com.android.support.*` |
| 2 | `kotlin-kapt` + artifact Glide KSP | Trung | Cấu hình sai; processor có lẽ không sinh gì |
| 3 | `org.apache.commons:commons-io:1.3.2` | Cao | GAV sai / đóng băng 2007. Phải đổi sang `commons-io:commons-io:2.22.0` |
| 4 | `mkloader` abandoned 2017 | Cao (UI) | Dính layout. **Pin**, không thay widget |
| 5 | `material-dialogs` 0.9.6.0 abandoned | Trung (UI) | 2 file Java. Pin 0.9.x |
| 6 | `android-processes` abandoned + vô dụng API 24+ | Thấp | Dead dep — ứng viên xóa (không đổi UI) |
| 7 | Picasso deprecated + unused | Thấp | Dead dep |
| 8 | `android-gif-drawable` unused + native `.so` | Cao (API 36 16KB) | Dead dep kéo native. Nên xóa trước khi target 36 |
| 9 | `Loader` / `AsyncTaskLoader` | Trung | `AppListLoader`, `MainActivity`, `AppsInstalledFragment`. Chưa removed, nhưng là API cũ |
| 10 | `AsyncTask` | Trung | `ShareTask extends AsyncTask` |
| 11 | `registerReceiver` không flag | **Cao — bug hiện tại** | `PackageReceiver` đăng ký runtime, `targetSdk` đã 34. API 34+ **bắt buộc** `RECEIVER_EXPORTED` / `NOT_EXPORTED` |
| 12 | `@OnLifecycleEvent` deprecated | Trung | `MyApplication.onMoveToForeground` |
| 13 | `AppOpenAd.load(..., APP_OPEN_AD_ORIENTATION_PORTRAIT, …)` | Cao khi bump Ads ≥ 24 | API đã xóa ở SDK 24 |
| 14 | `Activity.onBackPressed()` | Trung | Rất nhiều fragment gọi deprecated API. Predictive back (API 35) |
| 15 | `FLAG_LAYOUT_NO_LIMITS` + `statusBarColor` / `navigationBarColor` | Cao (API 35/36, R1) | Splash full-bleed. Theme set màu system bar. **Không** có `WindowInsets` / `enableEdgeToEdge`. API 35 edge-to-edge bắt buộc; API 36 không opt-out |
| 16 | `screenOrientation="portrait"` cả 3 Activity | **Cao (API 37, R1)** | Opt-out large-screen bị gỡ khi target 37. `InterestingConfigChange` có hằng orientation-related |
| 17 | `requestLegacyExternalStorage=true` | Thấp | No-op từ API 30; app đã dùng `MANAGE_EXTERNAL_STORAGE` |
| 18 | `lintOptions` + `abortOnError false` + disable `ContentDescription` | Thấp | DSL cũ; lint đang nuốt lỗi |
| 19 | `minifyEnabled false` | Thấp | Proguard file trống. R8 chưa chạy |
| 20 | RenderScript (`BlurKit`) | Thấp | `android.renderscript` deprecated. `BlurLayout` **không gắn layout XML nào** → dead code |
| 21 | `kotlinx.coroutines.CoroutineScope` import trong `ViewExtenstion.kt` | Thấp | Import; **không** có dep coroutines trực tiếp |
| 22 | Gson + reflection | Thấp khi minify off | Google khuyên Kotlin Serialization trên Android |
| 23 | Không version catalog | Thấp (build hygiene) | Sẽ gom ở Phase 3 |
| 24 | JDK bytecode 1.8 | Thấp | Nên nâng 17 cho khớp toolchain |
| 25 | Không CI | Trung (lưới an toàn) | Mọi verify là local |
| 26 | `jcenter()` / Support Library trực tiếp / ButterKnife / Synthetics / GCM / Fabric / RxJava1 / Volley / Firebase / Compose compiler extension | — | **Không có** |

---

## 5. Rủi ro build

| Hạng mục | Có? | Rủi ro AGP 9 / Gradle 9 |
|---|---|---|
| `applicationVariants` / Variant API cũ | **Không** | Thấp. Vẫn dính built-in Kotlin: phải bỏ `id 'kotlin-android'` hoặc tạm `android.builtInKotlin=false` |
| `kotlinOptions { jvmTarget }` | **Có** | Phải chuyển `kotlin { compilerOptions }` nếu bật built-in Kotlin |
| `lintOptions` | **Có** | Đổi `lint { }` |
| `buildConfigField` tùy biến | Không | — |
| Product flavors / signingConfigs | Không | — |
| Dynamic feature | Không | — |
| `packagingOptions` cũ | Không | — |
| Proguard rules thủ công | File trống, minify off | Thấp |
| Groovy + KTS lẫn | Chỉ Groovy | AGP 9 vẫn nhận Groovy; DSL mới có khác tên |
| Data Binding | **Có, mọi layout bọc `<layout>`** | AGP 9 còn hỗ trợ. `kotlin-kapt` có thể không cần nếu gỡ Glide processor |
| Custom Gradle task/plugin | Không | — |
| Configuration cache / isolated projects | Chưa bật | Gradle 9 sẽ warn hơn |
| `plugins {}` vừa `buildscript.classpath` vừa `plugins` | **Có** | AGP khai **2 lần** (classpath 8.2.2 + plugins 8.2.2). Cần dọn khi nâng |

**AGP 9 opt-out tạm (nếu cần, TODO gỡ trước AGP 10):**

- `android.builtInKotlin=false`
- `android.newDsl=false`

App **không** dùng `applicationVariants` nên có thể **không cần** `newDsl=false`. Built-in Kotlin thì **có** đụng vì đang apply `kotlin-android` + `kotlin-kapt` + `kotlin-parcelize`.

---

## 6. Lưới an toàn hiện có

| Loại | Hiện trạng |
|---|---|
| Unit test | 1 file: `ExampleUnitTest.addition_isCorrect` (`2+2=4`) |
| Instrumentation | 1 file: assert package name |
| Screenshot / Roborazzi / Paparazzi | Không |
| Coverage | 0% nghiệp vụ |
| Lint baseline | Không |
| CI | Không |
| LeakCanary / StrictMode | Không |

**Kết luận:** lưới an toàn gần như không có. Mọi nâng cấp phải dựa vào **build + smoke tay**. Không được refactor rộng. Không được đổi UI. Screenshot baseline sẽ làm ở Phase 2.

---

## 7. Mặt cắt runtime (để test sau)

Ghi `ÁP DỤNG` / `KHÔNG` theo code hiện tại.

### 7.1 Quyền & storage

| Hạng mục | Áp dụng? | File / ghi chú |
|---|---|---|
| Permission runtime storage | **Có** | `SplashActivity`: API ≥ 30 → `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`; API < 30 → READ/WRITE |
| `MANAGE_EXTERNAL_STORAGE` | **Có** | Manifest + Splash. Play: app file manager — cần declaration |
| `READ/WRITE_EXTERNAL_STORAGE` | **Có** | Manifest. Với target ≥ 33 gần như bị ignore nếu đã có manage-all-files |
| `READ_MEDIA_*` (13+) | **Không khai** | Dựa vào all-files access |
| `requestLegacyExternalStorage` | Có trong manifest | No-op API 30+ |
| `QUERY_ALL_PACKAGES` | **Có** | List app đã cài. Play restricted |
| `REQUEST_INSTALL_PACKAGES` | **Có** | Mở APK qua `OpenFile` + `FileProvider` (`application/vnd.android.package-archive`) |
| `REQUEST_DELETE_PACKAGES` | **Có** | `AppsInstalledFragment` `Intent.ACTION_DELETE` |
| `CLEAR_APP_CACHE` | Khai (privileged) | `tools:ignore` — không có effect trên Play |
| `INTERNET` / `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE` | **Có** | Ads + `isInternetAvailable` |
| `AD_ID` | **Có** | Ads |
| Scoped storage / MediaStore | Một phần | Chủ yếu `java.io.File`. `VideoFragment` query `MediaStore.Video` duration |
| FileProvider | **Có** | `OpenFile`, `FileUtils.shareFiles`. `provider_path.xml` có `root-path` `/` (rộng) |

### 7.2 Platform behavior 34→37

| Hạng mục (checklist đề bài) | Áp dụng? | Chi tiết |
|---|---|---|
| Foreground service + type | **Không** | Không FGS |
| `RECEIVER_EXPORTED` / context-registered receiver | **Có** | `PackageReceiver.registerReceiver` không flag — **crash tiềm ẩn ngay target 34 trên API 34+** |
| Implicit intent nội bộ | Có `ACTION_VIEW` / `ACTION_DELETE` / share | Cần smoke |
| JobScheduler / WorkManager | **Không** | |
| Notification / custom RemoteViews / app widget | **Không** | |
| PendingIntent mutability | **Không thấy** | |
| Edge-to-edge API 35/36 | **Có — R1** | Theme system bars trắng; Splash `FLAG_LAYOUT_NO_LIMITS`; `Utils.setStatusBarColor` flag cũ; **không** WindowInsets. Banner dính `layout_alignParentBottom` — nav bar sẽ đè nếu edge-to-edge |
| `elegantTextHeight` API 35 | Có thể | TextView/EditText nhiều màn — đo chiều cao dòng khi target 35 |
| Predictive back API 35 | **Có** | Hàng loạt `onBackPressed()` |
| `ACCESS_MEDIA_LOCATION` | **Không** | |
| FGS dataSync timeout | **Không** | |
| Large-screen orientation API 36 (còn opt-out) / API 37 (hết opt-out) | **Có — R1, DỪNG hỏi trước khi sửa** | 3 Activity `portrait`. Không `resizeableActivity=false`. Không `maxAspectRatio` |
| Adaptive refresh rate | **Không** đặc thù | |
| 16KB page size native `.so` API 36 | **Có nếu giữ gif-drawable**; **Không nếu xóa dead dep** | `libpl_droidsonroids_gif.so` 4 ABI |
| Static final + reflection API 37 | **Không thấy** trong app code. Gson dùng reflection field — khác với sửa `static final` | Grep `getDeclaredField` / `setAccessible` / `Class.forName`: không |
| MessageQueue lock-free API 37 | **Không** reflect internals. Chỉ `new Handler(Looper.getMainLooper())` — OK | |
| RemoteViews memory 1.5× screen | **Không** | Không widget / custom notification |
| Local network permission API 37 | **Không** | Không mDNS/NSD/cast/IoT/printer. `isInternetAvailable` DNS `www.google.com` = internet, không LAN |
| Background audio | **Không** | App không phát audio; chỉ list file + `ACTION_VIEW` |
| Background Activity Launch / IntentSender | Có `startActivity` từ context `OpenFile` (Application/Activity context — cần test) | |
| Safer DCL native API 37 | **Không** load `.so` động. gif-drawable load lúc init nếu còn dep | |
| OTP / SMS / password | **Không** SMS. Có `EditText` search/folder name — không password field | |
| Accessibility `setTextChangeTypes` API 37 | Có nhiều `EditText` chuẩn — platform tự bật; không custom IME | |
| App memory limit (mọi app trên Android 17) | Có scan file đệ quy trên main/background executor — rủi ro OOM khi thư mục lớn | `MainFragment.getFiles` |
| IME visibility sau rotate | `MainActivity` `SOFT_INPUT_STATE_HIDDEN`. Nhiều search `EditText`. Portrait lock → ít rotate trên phone; **tablet API 37 sẽ rotate** | |
| ECH / cleartext / network security config | **Không** có `networkSecurityConfig`. Không `usesCleartextTraffic`. Ads HTTPS | |
| WebView | **Không** | |
| Biometric / BT / location / camera / audio playback | **Không** (BT string + share-to-bluetooth trong `ShareTask` là intent, không API BT) | |
| SplashScreen API 31 | Splash **tự viết** (`SplashActivity` + timer), không `core-splashscreen` | |
| Deeplink | Chỉ LAUNCHER. Không intent-filter VIEW/BROWSABLE | |

### 7.3 Ads (runtime)

- Banner adaptive: `AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize` — **deprecated ở Ads 25**, thay large anchored APIs.
- Interstitial: `AdmobHelp`
- App Open: `MyApplication.AppOpenAdManager` + orientation overload **removed ≥ 24**
- Test IDs ở `debug`; production IDs ở `release` `resValue`

### 7.4 Màn hình / luồng smoke (cho Phase 2+)

1. Cold start Splash → cấp All files access → vào Home  
2. Home 8 ô: Images, Audio, Videos, Zips, Apps, Document, Download, More  
3. Browse storage (internal + SD nếu có), tạo folder, copy/move/delete, properties  
4. Search  
5. Favourites add/remove (SQLite)  
6. Recent / Large files / Storage space  
7. Installed apps list (Loader + Glide preload) → uninstall  
8. Mở file: ảnh, mp3, mp4, pdf, apk (FileProvider)  
9. Share file (ShareTask + MaterialDialog)  
10. Settings  
11. Ads: banner đáy Main, interstitial mỗi N lần back, app-open khi resume  
12. Xoay / resize / split-screen trên **sw ≥ 600dp** (sau target 36/37)  
13. minSdk 24 device: quyền READ/WRITE, không MANAGE  

---

## 8. Quyết định BẮT BUỘC hỏi trước khi Phase 1/3

Không tự quyết (R4):

1. **`mkloader` abandoned** — pin 1.4.0 (khuyến nghị, giữ UI) vs thay ProgressBar (đổi UI, cấm trừ khi bạn duyệt).  
2. **`material-dialogs` 0.9.6.0** — pin vs migrate 3.3.0 (đổi API + visual) vs Dialog Material3 hệ thống (đổi visual).  
3. **Material Components 1.12.0 → 1.14.0** — Expressive opt-in nhưng vẫn R1. Pin 1.12.0 hay lên 1.14.0 + screenshot?  
4. **Glide 4.14.2 → 5.0.9** (major) vs dừng 4.16.0 (last 4.x, 2023-08-21). Không có GlideModule.  
5. **kapt → KSP** (hay **gỡ processor Glide** vì không có `@GlideModule`).  
6. **AGP 9 built-in Kotlin ngay** vs tạm `android.builtInKotlin=false` rồi migrate commit riêng.  
7. **Gradle 9.6.1** (khớp AGP 9.4 default 9.6.x) vs **9.7.1** (latest; Kotlin 2.4.20 ghi hỗ trợ tới 9.7.0).  
8. **Ads 25.4.0** (docs) vs **25.5.0** (Maven hôm qua, chưa thấy rel-notes công khai).  
9. **Xóa dead deps** (Picasso, gif-drawable, android-processes) — không đổi UI; gif-drawable còn đụng 16KB.  
10. **Loader/AsyncTask** — giữ (ít rủi ro UI) vs migrate (nhiều file, hỏi vì R4 >20 file có thể).  
11. **targetSdk 37 vs rollback 36** — sẽ đánh giá ở Phase 5; ngay từ giờ: 3 Activity lock portrait là blocker UX duy nhất rõ ràng.

---

## 9. Đề xuất target (nháp — chốt ở Phase 1)

| | Đề xuất | Lý do |
|---|---|---|
| compileSdk | **37** | AGP 9.4 hỗ trợ max 37. Docs Android 17 |
| targetSdk | **37 từng nấc 34→35→36→37** | Rollback 36 được phép |
| minSdk | **24 giữ** | Không cắt user |
| Gradle | **9.6.1** (an toàn theo bảng AGP) hoặc 9.7.1 nếu bạn duyệt | |
| AGP | **9.4.0** (không 9.5 alpha) | |
| Kotlin | **2.4.20** | |
| KSP | **2.3.11 hoặc 2.3.12** nếu còn processor | |
| JDK / jvmTarget | **17** | |

---

## 10. Nguồn đã tra (Phase 0)

- https://developer.android.com/about/versions/17/setup-sdk  
- https://developer.android.com/about/versions/17/behavior-changes-17  
- https://developer.android.com/about/versions/17/migration  
- https://developer.android.com/blog/posts/android-17-is-here  
- https://developer.android.com/build/releases/agp-9-4-0-release-notes  
- https://developer.android.com/build/releases/about-agp  
- https://developer.android.com/build/migrate-to-built-in-kotlin  
- https://developer.android.com/agents/skills/build/agp/agp-9-upgrade/skill  
- https://docs.gradle.org/9.7.1/release-notes.html  
- https://gradle.org/releases/  
- https://kotlinlang.org/docs/whatsnew2420.html  
- https://kotlinlang.org/docs/releases.html  
- https://developer.android.com/jetpack/androidx/releases/appcompat  
- https://developer.android.com/jetpack/androidx/releases/constraintlayout  
- https://developer.android.com/jetpack/androidx/versions  
- https://developer.android.com/jetpack/androidx/releases/test  
- https://developers.google.com/admob/android/sdk  
- https://developers.google.com/admob/android/rel-notes  
- https://developers.google.com/admob/android/migration  
- Maven metadata: Google (`dl.google.com`) + Maven Central (`repo1.maven.org`) cho từng artifact ở §3  

---

**Phase 0 xong. Không có commit. Không có thay đổi code ngoài file inventory này. Chờ review rồi mới sang Phase 1 (plan).**
