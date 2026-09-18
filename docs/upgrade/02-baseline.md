# Phase 2 — Pre-upgrade baseline

**Ngày đo:** 2026-09-18  
**Branch:** `chore/upgrade-20260918`  
**Tag code gốc:** `pre-upgrade-baseline` → `d8751ef` (`initial code`)  
**JDK:** Temurin 17.0.19  
**Gradle:** 8.2 (wrapper) · AGP 8.2.2 · Kotlin 1.9.10  
**SDK:** compile/target 34 · min 24  
**Máy:** `VL-HCM-LAP-J4NVK.local` · `--no-daemon`

Mọi commit sau so với mốc này: **không được tệ hơn** (build phải xanh nếu baseline xanh; test không giảm; lint error không tăng; APK size / method không nở vô lý ngoài dep mới).

---

## 1. `./gradlew assembleDebug`

| | |
|---|---|
| Kết quả | **PASS** |
| Gradle wall | **2m 9s** (`BUILD SUCCESSFUL in 2m 9s`) |
| `/usr/bin/time` real | **130.72s** |
| APK | `app/build/outputs/apk/debug/app-debug.apk` **11 842 518 B** (11.29 MiB) |

Cảnh báo native: `Unable to strip the following libraries, packaging them as they are: libpl_droidsonroids_gif.so.`

---

## 2. `./gradlew testDebugUnitTest`

| | |
|---|---|
| Kết quả | **PASS** |
| Gradle wall | **8s** (`real 9.20s`) |
| Tests | **1 / 1** pass · 0 fail · 0 skip · 0 error |
| Suite | `com.syspolrer.systemfileexplorer.ExampleUnitTest.addition_isCorrect` |

Không có test nghiệp vụ. Instrumentation (`androidTest`) **không chạy** trong baseline này (cần device/emulator + cài app).

---

## 3. `./gradlew lintDebug`

| | |
|---|---|
| Kết quả | **PASS** (task thành công vì `abortOnError false`) |
| Gradle wall | **26s** (`real 27.34s`) |
| Tool | lint 8.2.2 |
| Report | `app/build/reports/lint-results-debug.html` |
| Tổng | **2 errors, 512 warnings** |

`ContentDescription` đang bị `disable` trong `app/build.gradle`.

### Errors (2)

| ID | File | Ghi chú |
|---|---|---|
| `MissingConstraints` | `app/src/main/res/layout/image_folder_item.xml:25` (`ImageView`) | Không vertical constraint — **không sửa XML** (R1). Baseline nợ. |
| `NotificationPermission` | Manifest (từ Glide `NotificationTarget`) | Target 34 thiếu `POST_NOTIFICATIONS`. App không tự post notification. |

### Warnings theo ID (512)

| ID | Count |
|---|---|
| DefaultLocale | 255 |
| UseCompoundDrawables | 64 |
| IconLocation | 43 |
| NotifyDataSetChanged | 26 |
| SetTextI18n | 17 |
| Autofill | 14 |
| TextFields | 14 |
| InefficientWeight | 9 |
| UseCompatLoadingForDrawables | 8 |
| DisableBaselineAlignment | 7 |
| GradleDependency | 7 |
| ObsoleteSdkInt | 7 |
| UselessParent | 7 |
| HardcodedText | 6 |
| StaticFieldLeak | 6 |
| SimpleDateFormat | 4 |
| LockedOrientationActivity | 3 |
| InlinedApi | 2 |
| Overdraw | 2 |
| ButtonCase | 1 |
| CustomSplashScreen | 1 |
| ExtraText | 1 |
| OldTargetApi | 1 |
| Recycle | 1 |
| RelativeOverlap | 1 |
| RtlSymmetry | 1 |
| ScopedStorage | 1 |
| Typos | 1 |
| VectorPath | 1 |
| VectorRaster | 1 |

`LockedOrientationActivity` (3) = 3 Activity `portrait` — liên quan API 37.  
`OldTargetApi` / `ScopedStorage` = target 34 + `MANAGE_EXTERNAL_STORAGE`.

---

## 4. Compiler warnings

### Kotlin (7) — `compileDebugKotlin`

| File | Warning |
|---|---|
| `MyApplication.kt:10` | `'OnLifecycleEvent' is deprecated` |
| `MyApplication.kt:42` | `'open' has no effect in a final class` |
| `MyApplication.kt:62` | `'OnLifecycleEvent' is deprecated` |
| `MyApplication.kt:159` | `AppOpenAd.load(..., Int, ...)` deprecated |
| `AppsRecyclerAdapter.kt:251` | Variable `themedActivity` never used |
| `AppsRecyclerAdapter.kt:514` | Unchecked cast `Any!` → `List<AppDataParcelable>` |
| `SpecialViewHolder.kt:31` | Parameter `c` never used |

### Java (`compileDebugJavaWithJavac`)

Không đếm từng dòng (chưa `-Xlint`). Javac ghi:

- `Some input files use or override a deprecated API.`
- `Some input files use unchecked or unsafe operations.`

Baseline: **7 Kotlin warnings + 2 Java compiler notes** (deprecation + unchecked, unenumerated).

---

## 5. APK / AAB / method / native

`assembleRelease` **PASS** in 36s (`real 37.03s`). Không có signing config → **unsigned** APK. Không build AAB (không `bundleRelease` config thêm).

| Artifact | Bytes | Ghi chú |
|---|---|---|
| `app-debug.apk` | 11 842 518 | 11.29 MiB |
| `app-release-unsigned.apk` | 9 890 611 | 9.43 MiB |
| Download size (apkanalyzer, release) | 8 508 476 | 8.11 MiB |

**Dex method references** (`apkanalyzer dex references`, release):

| File | Refs |
|---|---|
| classes.dex | 64 733 |
| classes2.dex | 65 434 |
| classes3.dex | 12 012 |
| **Tổng** | **142 179** |

`apkanalyzer dex packages --defined-only` `<TOTAL>`: **119 332** defined / **136 316** (cột 2) — dùng **142 179 refs** làm mốc so sánh chính.

**Native (release APK):**

- `/lib/{armeabi-v7a,arm64-v8a,x86,x86_64}/libpl_droidsonroids_gif.so`
- Merged manifest: `android:extractNativeLibs="false"`

Multidex: 3 dex files (Ads + AndroidX). Không cấu hình multidex thủ công — library multidex.

---

## 6. Merged manifest permissions (release)

Nguồn: `app/build/intermediates/merged_manifests/release/AndroidManifest.xml`  
`minSdk=24` · `targetSdk=34` · `versionCode=1` · `versionName=1.0`

**Khai trong app:**

- `CLEAR_APP_CACHE`
- `WRITE_EXTERNAL_STORAGE`
- `READ_EXTERNAL_STORAGE`
- `MANAGE_EXTERNAL_STORAGE`
- `REQUEST_INSTALL_PACKAGES`
- `QUERY_ALL_PACKAGES`
- `REQUEST_DELETE_PACKAGES`
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `ACCESS_WIFI_STATE`
- `com.google.android.gms.permission.AD_ID`

**Merge từ thư viện (Ads / AndroidX):**

- `ACCESS_ADSERVICES_AD_ID`
- `ACCESS_ADSERVICES_ATTRIBUTION`
- `ACCESS_ADSERVICES_TOPICS`
- `WAKE_LOCK`
- `FOREGROUND_SERVICE`
- `com.syspolrer.systemfileexplorer.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (signature, AndroidX)

**Tổng unique `uses-permission`: 17**

Không có `POST_NOTIFICATIONS` (lint error từ Glide).  
Không có `READ_MEDIA_*`.

Activities: cả 3 `screenOrientation="portrait"`. FileProvider + Ads APPLICATION_ID meta-data.

---

## 7. Screenshot UI

**Không chụp trong Phase 2.**

- AVD `Pixel_6_Pro` có trên SDK, **không** đang chạy.
- Device USB `R5CT41VEVFW` đang `device` — **không cài APK** lên máy thật (tránh đụng máy user).
- App cần `MANAGE_EXTERNAL_STORAGE` trước khi vào Home → không smoke tự động.

Khi có emulator/máy test: chụp Splash, Home, một list thumbnail, share dialog, Main+banner — lưu `docs/upgrade/screenshots/baseline/`.

---

## 8. Gate so sánh (commit sau)

| Metric | Baseline |
|---|---|
| assembleDebug | PASS · ~129–131s cold `--no-daemon` |
| testDebugUnitTest | 1/1 PASS |
| lintDebug | 2 err / 512 warn |
| Kotlin compiler warnings | 7 |
| Java compiler notes | 2 (deprecation, unchecked) |
| debug APK | 11 842 518 B |
| release unsigned APK | 9 890 611 B |
| dex method refs | 142 179 |
| merged uses-permission | 17 |
| native `.so` | gif-drawable × 4 ABI |

Kỳ vọng sau upgrade: Ads 25 có thể **tăng** APK/method; xóa gif-drawable **giảm** native + size. Lint error không được tăng. Test 1/1 phải giữ. Permission **mới** trên merged manifest → báo cáo trước khi ship.
