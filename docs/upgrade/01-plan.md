# Phase 1 — Plan nâng toolchain / SDK

**Ngày:** 2026-09-18  
**Baseline inventory:** `docs/upgrade/00-inventory.md`  
**Phạm vi:** chỉ plan. Không sửa code cho tới khi plan được duyệt.

Duyệt plan này = duyệt các **khuyến nghị mặc định** ở §1 và §5. Mục đánh dấu **DỪNG / HỎI** vẫn dừng khi tới lúc, dù plan đã duyệt.

---

## 1. Đề xuất target

| | Giá trị | Ảnh hưởng user |
|---|---|---|
| **compileSdk** | **37** | Không đổi hành vi runtime. Chỉ mở API 37 lúc compile. |
| **targetSdk** | **37**, nâng từng nấc 34 → 35 → 36 → 37 | Kích hoạt behavior change. Play đã đòi ≥ 36 từ 31/08/2026; 37 bắt buộc ~08/2027. Rollback hợp lệ: `targetSdk=36` + giữ `compileSdk=37`. |
| **minSdk** | **24 — giữ nguyên** | Không cắt máy Android 7.0. Material 1.14 / Ads 24+ chỉ đòi min 23. |

Không nâng minSdk trong đợt này.

---

## 2. Thứ tự nâng cấp (theo lớp)

Lý do thứ tự: mỗi lớp cô lập một class lỗi. Build lại sau mỗi lớp trong Phase 3; nếu gãy thì biết lớp nào.

| # | Lớp | Version chốt | Lý do |
|---|---|---|---|
| 1 | JDK / bytecode | Temurin 17 (đã có trên máy) + `jvmTarget=17` + `JavaVersion.VERSION_17` | AGP 9.4 min JDK 17. Bytecode 1.8 là nợ; không đụng UI. |
| 2 | Gradle wrapper | **9.6.1** (2026-06-26) | AGP 9.4.0 min/default **9.6.0**. 9.6.1 là patch cùng dòng. **Không** 9.7.1: Kotlin 2.4.20 test chính thức tới 9.7.0; AGP 9.4 không document 9.7. |
| 3 | AGP | **9.4.0** (2026-09-01) | Stable. Max API 37. Không dùng `9.5.0-alpha06`. |
| 4 | Kotlin | **2.4.20** (2026-09-07) | Khớp parcelize plugin 2.4.20. |
| 5 | KSP | **Không thêm** (xem §5.1) | Gỡ kapt vì Glide không có `@GlideModule`. Không cần KSP. |
| 6 | Plugin khác | `org.jetbrains.kotlin.plugin.parcelize` 2.4.20 | Bỏ `kotlin-android` khi migrate built-in Kotlin. Không Hilt/Firebase/safeargs. |
| 7 | BOM | **Không có** Compose/Firebase/OkHttp BOM | Ads không có BOM riêng ta đang dùng. |
| 8 | AndroidX / Google | Appcompat 1.8.0 · Material **1.14.0** (cổng visual) · ConstraintLayout 2.2.2 · Ads **25.4.0** | Material: theme **giữ** `Theme.Material3.Light.NoActionBar` (không Expressive). Ads: docs latest = 25.4.0, không 25.5.0 (Maven có, chưa rel-notes). |
| 9 | Third-party | Glide **5.0.9** · Gson 2.14.0 · commons-io **2.22.0** (đổi GAV) · pin mkloader 1.4.0 · pin material-dialogs 0.9.6.0 · **xóa** Picasso / gif-drawable / android-processes | Dead dep không đổi UI. gif-drawable còn `.so` → 16KB. |
| 10 | Test | junit 4.13.2 (giữ) · androidx.test.ext:junit 1.3.0 · espresso 3.7.0 | Không nhảy JUnit 5. |
| 11 | compileSdk | **37**, targetSdk **giữ 34** | Tách lỗi toolchain vs behavior. |
| 12 | targetSdk | 34→35→36→37, mỗi nấc 1 commit | Crash thì biết level. |

**Cờ AGP 9 tạm trong commit nâng version** (TODO gỡ ở commit fix ngay sau):

```
android.builtInKotlin=false
android.newDsl=false
```

Lý do: Phase 3 cấm refactor nghiệp vụ; AGP 9 default bẻ `kotlin-android` / `kotlinOptions` / `lintOptions`. Opt-out là đúng spec. Gỡ opt-out ở commit `fix(build): migrate to AGP 9 built-in Kotlin`.

**Không** thêm Compose compiler plugin.

---

## 3. Danh sách commit dự kiến

Branch: `chore/upgrade-20260918`  
Tag gốc (Phase 2): `pre-upgrade-baseline`

Rủi ro: T = thấp, M = trung, C = cao. Rollback = `git revert <hash>` trừ khi ghi khác.

### Phase 2 — Baseline

| # | Title | Phạm vi | Rủi ro | Rollback |
|---|---|---|---|---|
| 0 | `chore(baseline): record pre-upgrade build, lint and APK metrics` | `docs/upgrade/02-baseline.md` (+ screenshot nếu emulator chạy được) | T | Xóa file docs |

### Phase 3 — Upgrade (1 commit, được phép FAIL)

| # | Title | Phạm vi | Rủi ro | Rollback |
|---|---|---|---|---|
| 1 | `chore(deps): upgrade toolchain and all dependencies to latest stable` | Wrapper 8.2→9.6.1; AGP 8.2.2→9.4.0; Kotlin 1.9.10→2.4.20; JDK bytecode 8→17; `libs.versions.toml`; compileSdk 34→37; **targetSdk giữ 34**; bump AndroidX/Ads/Glide/Gson/test; đổi GAV commons-io; xóa Picasso/gif-drawable/android-processes; pin mkloader + material-dialogs; opt-out AGP 9; dọn `buildscript` trùng; `lintOptions`→`lint` nếu AGP bắt. **Không sửa Java/Kotlin nghiệp vụ.** | C | revert commit |

Body commit 1 sẽ liệt kê version + link. Nếu FAIL (Ads API, AGP DSL): vẫn commit, lỗi sửa ở Phase 4.

### Phase 4 — Fix theo nguyên nhân gốc

| # | Title | Phạm vi dự kiến | Rủi ro | UI? |
|---|---|---|---|---|
| 2 | `fix(build): migrate to AGP 9 built-in Kotlin` | Bỏ `kotlin-android`; giữ `org.jetbrains.kotlin.plugin.parcelize`; bỏ kapt (không còn processor); `kotlinOptions` → `compilerOptions` nếu còn; **gỡ** `android.builtInKotlin=false` / `android.newDsl=false` nếu build xanh | M | Không |
| 3 | `fix(ads): migrate AppOpenAd and banner APIs to GMA 25` | `MyApplication.kt` bỏ orientation overload; banner size deprecated → API mới **cùng kích thước portrait phone** (không đổi layout XML). `AdmobHelp` nếu đụng API removed | M | Ads size: cổng visual. Nếu large-banner khác chiều cao → hỏi / pin cách gọi cũ nếu còn |
| 4 | `fix(api): register PackageReceiver with RECEIVER_NOT_EXPORTED` | `PackageReceiver.java` — bug **hiện tại** (target đã 34) | T | Không |
| 5 | `fix(lifecycle): replace OnLifecycleEvent with DefaultLifecycleObserver` | `MyApplication.kt` | T | Không |
| 6 | `fix(api): replace deprecated onBackPressed with OnBackPressedDispatcher` | Nhiều fragment gọi `requireActivity().onBackPressed()`. **Chỉ đổi lời gọi**, không đổi flow back. Nếu >20 file: **DỪNG hỏi** trước khi commit (R4) | M | Không nếu chỉ thay API |
| 7 | (dự phòng) `fix(io): adapt Apache Commons IO 2.22 FileUtils signatures` | Chỉ nếu 2.22 gãy `copyFileToDirectory`/`deleteDirectory` | T | Không |
| 8 | (dự phòng) `fix(glide): adapt Glide 5 request API` | Chỉ nếu 5.0.9 gãy compile | T | Thumbnail: so baseline |

Không migrate Loader/AsyncTask trong đợt này.

### Phase 5 — targetSdk từng nấc

Đọc trước mỗi nấc: `behavior-changes-<N>` **và** `behavior-changes-all`.

| # | Title | Việc bắt buộc trước khi bump | Rủi ro |
|---|---|---|---|
| 9 | `fix(insets): restore visual parity for edge-to-edge` | **Hỏi trước khi sửa** (R1 ngoại lệ). Approach: `WindowInsets` / `ViewCompat.setOnApplyWindowInsetsListener` trên root Splash + Main + SelectFile; padding status/nav **bù đúng** theme hiện tại (bar trắng, banner không bị nav bar đè). Splash đang `FLAG_LAYOUT_NO_LIMITS` — giữ visual full-bleed. **Không** đổi XML hierarchy, màu, dimens. | C / R1 |
| 10 | `feat(sdk): target Android 15 (API 35)` | Sau insets. Checklist 5.1 API 35: edge-to-edge, elegantTextHeight (đo TextView nếu lệch), predictive back (commit 6), không FGS/media location | C |
| 11 | `feat(sdk): target Android 16 (API 36)` | 16KB: **đã xóa gif-drawable** → N/A. Edge-to-edge không opt-out. Large-screen 36 vẫn còn opt-out: **không** thêm opt-out mới; chỉ quan sát | C |
| 12 | *(báo cáo, không commit)* Rà API 37 large-screen | Liệt kê 3 Activity `portrait`. **DỪNG.** Không sửa `screenOrientation` / không adaptive layout trừ khi bạn duyệt. | C / R1 |
| 13 | `feat(sdk): target Android 17 (API 37)` | Chỉ sau bạn duyệt §12. Nếu blocker UX màn lớn: rollback `targetSdk=36`, ghi `docs/upgrade/blockers-api37.md` | C |

Không có commit riêng cho: reflection static final, MessageQueue, RemoteViews, LAN permission, background audio, DCL native, OTP/password — **không áp dụng**.

`usesCleartextTraffic` / network security config: không bắt buộc để target 37 (chưa deprecate thành error). Đề xuất Phase 7, không làm.

### Phase 6 — Warning (mỗi loại 1 commit)

| # | Title |
|---|---|
| 14 | `chore(lint): fix Android lint errors` |
| 15 | `chore(lint): fix Android lint warnings` |
| 16 | `chore(kotlin): fix deprecation warnings` |
| 17 | `chore(kotlin): fix remaining compiler warnings` |
| 18 | `chore(gradle): fix Gradle deprecation warnings` |
| 19 | `chore(lint): enable warningsAsErrors to prevent regressions` | **Không làm** trừ khi bạn duyệt |

RenderScript `BlurKit` (dead, không gắn layout): chỉ đụng nếu lint/compiler bắt — xóa dead code **không đổi UI**; nếu đụng sẽ hỏi vì không nằm trong commit dự kiến.

### Phase 7

| # | Title |
|---|---|
| 20 | `docs(upgrade): final report vs baseline` | `docs/upgrade/99-report.md` |

---

## 4. Breaking change dự đoán

| Thay đổi | Chỗ đụng | Cách sửa (đợt này) |
|---|---|---|
| AGP 9 built-in Kotlin; `kotlin-android` incompatible | `build.gradle`, `app/build.gradle` | Commit 1: opt-out. Commit 2: bỏ plugin, parcelize riêng, gỡ opt-out |
| AGP 9 new DSL; `lintOptions` / `kotlinOptions` | `app/build.gradle` | Commit 1: `android.newDsl=false` hoặc đổi `lint {}` / `compilerOptions` nếu AGP bắt |
| kapt incompatible built-in Kotlin | `kotlin-kapt` + `kapt glide:ksp` | **Gỡ kapt + gỡ Glide processor** (không có `@GlideModule`). Không KSP |
| GMA 24+: `AppOpenAd.load` bỏ orientation | `MyApplication.kt` ~159–164 | Overload 4 tham số. [migration](https://developers.google.com/admob/android/migration) |
| GMA 25: banner size cũ deprecated | `MainActivity.initAdsBanner` | Đổi method size; **giữ** inline adaptive hiện tại nếu vẫn compile; nếu removed thì API mới + đo chiều cao banner vs baseline |
| `@OnLifecycleEvent` deprecated | `MyApplication` | `DefaultLifecycleObserver` |
| `registerReceiver` không flag (API 34) | `PackageReceiver.java` | `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)` — package/sdcard là broadcast hệ thống, không export |
| `onBackPressed()` deprecated | ~20+ fragment | `OnBackPressedDispatcher` — **DỪNG nếu >20 file** |
| commons-io GAV 1.3.2 → 2.22.0 | 7 fragment `FileUtils.*` | Cùng package `org.apache.commons.io`. Nếu signature đổi: commit 7 |
| Glide 5 | adapters dùng `Glide.with` + `RequestOptions` | Kỳ vọng compile; nếu không: commit 8. So thumbnail vs baseline |
| Material 1.14 Expressive | `themes.xml` parent `Theme.Material3.Light.NoActionBar` | **Không** đổi sang `Theme.Material3Expressive`. Screenshot. Lệch visual → pin **1.12.0** (regression R1) |
| compileSdk 37 | không | Chỉ SDK |
| API 35 edge-to-edge | Splash `FLAG_LAYOUT_NO_LIMITS`; theme `statusBarColor`/`navigationBarColor` white; Main banner `alignParentBottom`; `Utils.setStatusBarColor` | Insets padding, **không** redesign. Commit 9, hỏi trước |
| API 35 elegantTextHeight | TextView/EditText nhiều màn | Đo. Lệch chiều cao dòng → `android:elegantTextHeight=false` theo view nếu Google cho phép, **hỏi trước** vì đụng text metrics |
| API 35 predictive back | back stack fragment | Commit 6 |
| API 36 16KB `.so` | chỉ gif-drawable | **Xóa dep** ở commit 1 → không còn `.so` |
| API 36/37 large screen ignore `screenOrientation` | 3 Activity `portrait` | **Không sửa trong plan này.** Báo cáo rồi hỏi (commit 12 gate) |
| API 37 reflection / MQ / RemoteViews / LAN / audio / DCL / OTP | — | Không áp dụng |
| `enableJetifier` | mkloader + dialogs 0.9 | **Giữ `true`**. Thử tắt sau khi dependencyInsight không còn `com.android.support`. Nếu tắt được: commit nhỏ `chore(gradle): disable jetifier` — chỉ khi xanh |

---

## 5. CONFLICT & CONCERN

### 5.1 Thư viện: thay hay không

#### A. `com.tuyenmonkey:mkloader:1.4.0` (abandoned 2017, ~14 layout)

| Lựa chọn | Phổ biến | Release | Chi phí | Rủi ro UI | Rollback |
|---|---|---|---|---|---|
| **Pin 1.4.0** (khuyến nghị) | Legacy | Chết | 0 | Không | n/a |
| `ProgressBar` hệ thống | Cao | Platform | ~14 XML + 11 adapter | **Có — cấm R1** | revert |
| Lottie / custom spinner | Trung | Tùy | Cao | Có | revert |

Cost of doing nothing: jetifier phải giữ; không CVE mới vì không mạng. **Làm: pin.**

#### B. `com.afollestad.material-dialogs:core:0.9.6.0`

| Lựa chọn | Phổ biến | Release | Chi phí | Rủi ro UI | Rollback |
|---|---|---|---|---|---|
| **Pin 0.9.6.0** (khuyến nghị) | Cũ | Chết ~2018 | 0 | Không | n/a |
| 3.3.0 (Kotlin rewrite) | Trung, chết 2021 | 2021-02-21 | `ShareTask` + `ShareAdapter` rewrite | **Có** (dialog 3.x khác 0.9) | revert |
| `MaterialAlertDialogBuilder` | Cao | Material 1.14 | 2 file | Có (style Material3) | revert |

Cost of doing nothing: jetifier; share dialog giữ nguyên. **Làm: pin.**

#### C. Glide 4.14.2

| Lựa chọn | Phổ biến | Release | Chi phí | Rủi ro UI | Rollback |
|---|---|---|---|---|---|
| **5.0.9** (khuyến nghị) | Cao | 2026-07-11 | Gỡ processor; adapters giữ `Glide.with` | Thấp (không GlideApp). Cổng thumbnail | pin 4.16.0 |
| Pin **4.16.0** last 4.x | Cao | 2023-08-21 | Version bump nhỏ | Rất thấp | 4.14.2 |
| Coil | Cao 2026 | Active | Nhiều adapter | Có | revert |

Cost of doing nothing: 4.14.2 vẫn chạy, lệch 2 năm so 4.16 / 3 năm so 5.x. **Làm: 5.0.9** vì không generated API; nếu thumbnail lệch → pin 4.16.0.

**Không thêm KSP.** Gỡ `kapt` + `glide:ksp`.

#### D. `org.apache.commons:commons-io:1.3.2`

Không phải “thay thư viện” — **sửa GAV sai** sang `commons-io:commons-io:2.22.0`. Không thay bằng Okio/Java NIO (đụng 7 file copy/delete).

#### E. Gson 2.11.0

| Lựa chọn | Chi phí | Rủi ro |
|---|---|---|
| **Bump 2.14.0** (khuyến nghị) | 1 dòng | Thấp. minify off |
| Kotlin Serialization | `StorageUtils` + model | Không đáng đợt này |
| Không bump | 0 | Nợ |

Cost of doing nothing: ổn. **Làm: bump 2.14.0**, không migrate.

#### F. Picasso / gif-drawable / android-processes

**Xóa.** Không usage. gif-drawable là native risk API 36. Không thay bằng Coil.

### 5.2 Hoãn sang đợt sau

| Hạng mục | Lý do |
|---|---|
| Groovy → KTS | Không cần để lên 37. Rối diff |
| Data Binding → ViewBinding | >20 file, không lợi SDK |
| Loader / AsyncTask → Coroutines | Nhiều file, không unblock 37 |
| JUnit 4 → 5 | Test không có giá trị; nhảy framework = nhiễu |
| Material Expressive theme | Đổi visual có chủ đích — R1 |
| R8 full mode / minify on | minify đang off; bật = rủi ro keep rules Ads/Glide |
| Tắt jetifier ngay commit 1 | Phụ thuộc pin 0.9 dialogs + mkloader |
| `core-splashscreen` | Đổi splash system — R1 |
| Adaptive layout tablet | UX mới — chỉ làm nếu bạn duyệt sau báo cáo API 37 |
| LeakCanary / screenshot CI / Dependabot | Phase 7 đề xuất, không làm |
| Gradle 9.7.1 | Lệch bảng tương thích AGP 9.4 |
| Ads 25.5.0 | Không có rel-notes công khai |
| warningsAsErrors | Hỏi ở Phase 6 |

### 5.3 Pin cứng (không “latest”)

| Artifact | Pin | Lý do |
|---|---|---|
| `com.tuyenmonkey:mkloader` | 1.4.0 | Abandoned + UI |
| `com.afollestad.material-dialogs:core` | 0.9.6.0 | Abandoned + UI |
| `com.google.android.material:material` | 1.14.0 **có điều kiện**; fallback **1.12.0** | Expressive / default component. Theme không đổi parent |
| `com.google.android.gms:play-services-ads` | **25.4.0** | Docs; không 25.5.0 |
| Gradle | **9.6.1** | Khớp AGP 9.4; không 9.7.1 |
| AGP | **9.4.0** | Không alpha 9.5 |
| junit | 4.13.2 | Không JUnit 5 |

### 5.4 Rủi ro targetSdk 37 trên **repo này**

Checklist 5.2 đề bài → áp dụng thật:

| # | Hạng mục | Áp dụng? | Việc làm |
|---|---|---|---|
| 1 | Resizability / orientation sw>600dp, hết opt-out | **CÓ** | 3 Activity `android:screenOrientation="portrait"`. Không `resizeableActivity=false` / maxAspect. App giả định portrait (ads open portrait, nhiều list). **DỪNG báo cáo trước mọi sửa.** |
| 2 | static final + reflection | Không (app). Gson không ghi `Build.SDK_INT` | Smoke Ads/Gson trên API 37 |
| 3 | MessageQueue lock-free | Không reflect | — |
| 4 | RemoteViews 1.5× | Không widget/custom notif | — |
| 5 | Local network permission | Không LAN | — |
| 6 | Background audio | Không | — |
| 7 | BAL / IntentSender | `OpenFile.startActivity` từ context giữ sẵn — test mở file khi app background | Không đổi API trừ crash |
| 8 | Safer DCL native | Không sau khi xóa gif | — |
| 9 | OTP / password | Không | — |
| 10 | Accessibility TextView | EditText chuẩn | Smoke search/create folder |

5.1 trung gian:

| Level | Áp dụng trong repo |
|---|---|
| 34 | **Receiver flags — bug hiện tại.** FGS/PendingIntent/Job: không |
| 35 | **Edge-to-edge + predictive back + elegantTextHeight (theo dõi)** |
| 36 | Edge-to-edge hết opt-out. 16KB: xử lý bằng xóa gif. Large-screen 36 còn opt-out: không thêm cờ |

5.3 mọi app trên Android 17 (kể cả target 36):

- Scan file đệ quy có thể OOM → quan sát, không refactor scanner
- IME sau rotate: quan trọng khi tablet hết portrait lock
- ECH / cleartext: không pin SNI, không cleartext config

**Chỗ dễ vỡ nhất:** (1) AGP 9 Kotlin/DSL, (2) Ads 23→25 compile, (3) edge-to-edge vs banner đáy, (4) tablet API 37 bỏ portrait.

---

## 6. Kế hoạch verify

### Lệnh

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug --warning-mode=all   # Phase 6
```

Không `bundleRelease` signed (không có signing config). APK debug metrics đủ so baseline. Nếu cần size: `assembleRelease` unsigned (`minifyEnabled false`).

Sau mỗi commit Phase 3–5: assembleDebug + testDebugUnitTest. Lint đầy đủ ở Phase 2 và Phase 6.

### Smoke tay (luồng app này, không generic)

1. Cold start → Splash → cấp All files access (API 30+) hoặc READ/WRITE (API 24) → Home  
2. Home 8 ô: Images, Audio, Videos, Zips, Apps, Document, Download, More  
3. Storage: tạo folder, copy, move, delete, file info  
4. Search  
5. Favourite add/remove (SQLite)  
6. Recent / Large files / Storage space  
7. Installed apps (Loader + Glide) → uninstall  
8. Mở ảnh / mp3 / mp4 / pdf / apk (FileProvider)  
9. Share (MaterialDialog 0.9)  
10. Settings  
11. Ads: banner đáy Main **không đè nội dung / không bị nav bar đè**; interstitial khi back (đếm `checkShowAdsFull`); app-open khi resume từ background  
12. MKLoader hiện/ẩn khi list load — **cùng hình spinner cũ**

So UI vs Phase 2 screenshot: Home, Splash, một list có thumbnail, một dialog share, Main có banner.

### Ma trận thiết bị

| | API 24 (min) | API 34 (target hiện tại) | API 35 | API 36 | API 37 |
|---|---|---|---|---|---|
| Phone nhỏ (~360dp) | bắt buộc | bắt buộc | khi bump 35 | khi bump 36 | khi bump 37 |
| Phone lớn | — | nên | — | — | bắt buộc |
| Tablet / foldable sw≥600 | — | baseline quan sát | — | quan sát (36 còn opt-out orientation) | **bắt buộc** xoay + resize + split — **trước** commit target 37 |

Không có emulator trên máy agent lúc Phase 0. Phase 2: thử; nếu không chạy được, ghi rõ trong `02-baseline.md` và nhờ bạn chụp.

---

## 7. Ước lượng

| | |
|---|---|
| Commit dự kiến | **~20** (0 baseline + 1 upgrade + ~6 fix + 1 insets + 3 target + 5 lint/warn + 1 report). Dự phòng +2 (IO/Glide). |
| Thời lượng | **6–10 giờ** engineering nếu AGP 9 + Ads compile xanh; **+1 vòng hỏi** cho insets và API 37 orientation. Calendar: 2–4 buổi vì **dừng sau mỗi bước**. |
| Dễ vỡ | AGP 9 built-in Kotlin / Data Binding; GMA 25 AppOpenAd; edge-to-edge + banner; tablet bỏ portrait. |
| Lưới an toàn | Gần 0 test thật → không refactor, không đổi XML, pin UI libs. |

---

## 8. Cổng duyệt plan (checklist)

Gửi **tiếp** sau khi đồng ý (hoặc sửa từng dòng):

- [ ] minSdk 24 giữ  
- [ ] compileSdk 37; targetSdk 37 từng nấc; rollback 36 được phép  
- [ ] Gradle **9.6.1** (không 9.7.1)  
- [ ] AGP **9.4.0** + opt-out built-in Kotlin/newDsl ở commit nâng version, gỡ ngay commit sau  
- [ ] Kotlin 2.4.20; **không** KSP; **gỡ kapt**  
- [ ] Ads **25.4.0** (không 25.5.0)  
- [ ] Material 1.14.0 + theme cũ; lệch visual → pin 1.12.0  
- [ ] Glide 5.0.9; lệch thumbnail → pin 4.16.0  
- [ ] Pin mkloader 1.4.0 và material-dialogs 0.9.6.0  
- [ ] Xóa Picasso, gif-drawable, android-processes  
- [ ] Đổi GAV commons-io → 2.22.0  
- [ ] Hoãn: KTS, ViewBinding, Loader/AsyncTask, JUnit 5, minify, Expressive, jetifier-off (trừ khi insight sạch)  
- [ ] Insets API 35: **vẫn hỏi lại** trước khi sửa file (R1)  
- [ ] target 37 orientation: **vẫn dừng báo cáo** trước khi sửa (R1)

---

## Docs đã tra thêm (Phase 1)

- https://docs.gradle.org/9.6.1/release-notes.html  
- https://developer.android.com/build/migrate-to-built-in-kotlin  
- https://developer.android.com/build/releases/agp-9-4-0-release-notes  
- https://developer.android.com/about/versions/15/behavior-changes-15 (sẽ đọc kỹ trước commit target 35)  
- https://developer.android.com/about/versions/16/behavior-changes-16  
- https://developer.android.com/about/versions/17/behavior-changes-17  
- https://developers.google.com/admob/android/migration  
- https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.parcelize (2.4.20)

**Phase 1 xong. Không đụng code. Chờ duyệt.**
