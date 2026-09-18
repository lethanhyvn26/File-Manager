# Phase 7 — Báo cáo nâng cấp vs baseline

**Ngày:** 2026-09-18  
**Branch:** `chore/upgrade-20260918`  
**Tag gốc:** `pre-upgrade-baseline` → `d8751ef`  
**HEAD:** `d178105`  
**Verify:** `./gradlew assembleDebug testDebugUnitTest lintDebug assembleRelease --warning-mode all --no-daemon` → **BUILD SUCCESSFUL in 24s**  
**APK targetSdk (aapt2 dump badging):** **37** · compileSdk **37** · minSdk **24**

Không push. Không `warningsAsErrors`. Không đổi layout XML / theme / copy.

---

## 1. Toolchain sau nâng cấp

| | Baseline | Sau nâng cấp |
|---|---|---|
| Gradle | 8.2 | **9.6.1** |
| AGP | 8.2.2 | **9.4.0** |
| Kotlin | 1.9.10 + `kotlin-android` + kapt | **2.4.20** built-in Kotlin, parcelize plugin, **không kapt** |
| JDK / bytecode | 17 host / 1.8 | 17 / **17** |
| compileSdk / targetSdk | 34 / 34 | **37 / 37** |
| Catalog | không | `gradle/libs.versions.toml` |

**Deps chốt:** Appcompat 1.8.0 · Material **1.14.0** (theme giữ `Theme.Material3.Light.NoActionBar`) · ConstraintLayout 2.2.2 · Ads **25.4.0** · Glide **5.0.9** · Gson 2.14.0 · commons-io **commons-io:commons-io:2.22.0** · JUnit 4.13.2 · test-ext 1.3.0 · Espresso 3.7.0.

**Pin (R1 / abandoned):** mkloader **1.4.0** · material-dialogs **0.9.6.0** · `android.enableJetifier=true`.

**Xóa dead deps:** Picasso, android-gif-drawable, android-processes.

Lint `NewerVersionAvailable` / `GradleDependency` / `AndroidGradlePluginVersion` còn báo Ads 25.5.0, dialogs 3.3.0, Gradle 9.7.1 — **cố ý không nhảy** (plan §2 / §5).

---

## 2. So baseline (gate)

| Metric | Baseline | Sau nâng cấp | Ghi chú |
|---|---|---|---|
| assembleDebug | PASS · ~2m 9s cold | **PASS** · 24s warm | Xanh |
| testDebugUnitTest | **1 / 1** | **1 / 1** | Không thêm test nghiệp vụ |
| lintDebug errors | 2 | **1** | `NotificationPermission` disable (Glide, không thêm permission). Còn `MissingConstraints` XML — R1 |
| lintDebug warnings | 512 | **243** | DefaultLocale / drawable / ObsoleteSdkInt / SimpleDateFormat / Recycle đã xử lý. Còn chủ yếu XML |
| Kotlin compiler warnings | 7 | **0** | |
| Java compiler notes | deprecation + unchecked | **deprecation only** (unchecked đã gỡ) | Loader / AsyncTask / RenderScript / FragmentPagerAdapter giữ |
| debug APK | 11 842 518 B | **13 555 389 B** | +1.71 MiB — Ads 25 + AndroidX |
| release unsigned | 9 890 611 B | **11 014 531 B** | +1.07 MiB |
| download size (release) | 8 508 476 | **9 480 195** | |
| dex method refs | 142 179 (3 dex) | **159 557** (3 dex: 56048+59868+43641) | Ads/AndroidX |
| merged `uses-permission` | 17 | **17** | **Không** `POST_NOTIFICATIONS`. Không `READ_MEDIA_*` |
| native `.so` | gif-drawable × 4 ABI | **0** | 16KB page-size: N/A |

Quyền merge (release) giữ nguyên tập baseline: storage / install / query packages / ads / network + Ads AdServices + `WAKE_LOCK` + `FOREGROUND_SERVICE` + AndroidX dynamic-receiver.

---

## 3. Commits (mỗi step một commit)

| Hash | Title |
|---|---|
| `700a095` | `chore(baseline): record pre-upgrade build, lint and APK metrics` |
| `530ade2` | `chore(deps): upgrade toolchain and all dependencies to latest stable` (được phép FAIL) |
| `6839194` | `fix(ads): migrate AppOpenAd.load off removed orientation API` |
| `bdaca1b` | `fix(build): migrate to AGP 9 built-in Kotlin and drop kapt` |
| `e790d51` | `fix(api): register PackageReceiver with RECEIVER_NOT_EXPORTED` |
| `22c444a` | `fix(lifecycle): replace OnLifecycleEvent with DefaultLifecycleObserver` |
| `b4f6f7c` | `fix(lifecycle): disambiguate Application.onCreate super call` |
| `384a62e` | `fix(api): replace Activity.onBackPressed with OnBackPressedDispatcher` |
| `96daa29` | `fix(api): resolve OnBackPressedDispatcher on FragmentActivity` |
| `0bb9656` | `fix(insets): restore visual parity for edge-to-edge` |
| `85a5456` | `feat(sdk): target Android 15 (API 35)` |
| `171c66b` | `feat(sdk): target Android 16 (API 36)` |
| `638e701` | `feat(sdk): target Android 17 (API 37)` |
| `639ea4c` | `chore(lint): fix Android lint errors` |
| `5ce698d` | `chore(lint): fix Android lint DefaultLocale warnings` |
| `80b4749` | `chore(lint): fix remaining non-UI Android lint warnings` |
| `7caae51` | `chore(kotlin): fix remaining compiler warnings` |
| `eb9d309` | `chore(kotlin): replace remaining safe Java deprecations` |
| `efcbe21` | `chore(kotlin): fix remaining Java compiler unchecked warnings` |
| `3f05a7d` | `chore(gradle): drop deprecated android.nonFinalResIds=false` |
| `d178105` | `chore(gradle): replace Groovy space-assignment before Gradle 10` |

`chore(lint): enable warningsAsErrors` — **không làm**.

---

## 4. Hành vi platform đã adapt (không đổi UI)

- **GMA 25:** `AppOpenAd.load` 4 tham số (bỏ orientation). Banner vẫn `AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize` (API inline **không** deprecated; anchored mới deprecated).
- **API 34+ receiver:** `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)`.
- **Lifecycle:** `DefaultLifecycleObserver`; `super<Application>.onCreate()`.
- **Back:** `OnBackPressedDispatcher` / `getBindingAdapterPosition`.
- **API 35 edge-to-edge:** `Utils.applySystemBarInsets` trên Main + SelectFile (padding systemBars). Splash giữ `FLAG_LAYOUT_NO_LIMITS`.
- **API 36 16KB:** không còn `.so`.
- **Predictive back:** lời gọi `onBackPressed()` cũ đã thay; không thêm animation tùy biến.

---

## 5. API 37 large-screen (báo cáo, không sửa XML)

Ba Activity đều `android:screenOrientation="portrait"`:

- `SplashActivity`
- `MainActivity`
- `SelectFileActivity`

Từ Android 16, orientation lock **bị bỏ qua** trên hầu hết large screen (sw ≥ 600dp). Không có `resizeableActivity=false` / maxAspect opt-out mới. App layout + banner giả định portrait.

**Không** đổi `screenOrientation`, không adaptive pane, không screenshot tablet — R1. Cần duyệt riêng nếu ship tablet/foldable.

Hạng mục API 37 khác (reflection static final, MessageQueue, RemoteViews, LAN, background audio, DCL native, OTP) **không áp dụng**.

---

## 6. Còn lại có chủ đích

| Hạng mục | Lý do giữ |
|---|---|
| `MissingConstraints` `image_folder_item.xml` | Sửa XML = đụng UI (R1) |
| UseCompoundDrawables / IconLocation / Autofill / TextFields / … | Layout XML |
| `NotifyDataSetChanged` / `StaticFieldLeak` | Hành vi list / leak sẵn có, không phải lỗi upgrade |
| `UseKtx` | Cần `core-ktx` explicit; không thêm dep |
| `LockedOrientationActivity` / `DiscouragedApi` | Portrait lock — §5 |
| `FragmentPagerAdapter` | ViewPager2 đổi tab UI |
| `AsyncTask` / `Loader` / `AsyncTaskLoader` | Plan: không migrate đợt này |
| RenderScript `BlurKit` | Dead, không gắn layout |
| `GET_SIGNATURES` trong `AppListLoader` | Nằm trong Loader |
| `Utils.setStatusBarColor` flags cũ | Có thể lệch visual status bar |
| `android.enableJetifier=true` | mkloader + material-dialogs 0.9. AGP cảnh báo gỡ ở AGP 10 — pin đến khi thay 2 lib |
| Ads 25.5.0 / Gradle 9.7.1 | Ngoài pin plan |
| Jetifier / configuration cache | Suggestion, không deprecation Gradle 10 còn lại sau `prop = value` |

---

## 7. Screenshot / smoke

**Không chụp.** Không cài APK lên USB `R5CT41VEVFW`. Emulator không chạy trong đợt này.

Khi có máy test: Splash, Home + banner đáy, list thumbnail Glide, share dialog 0.9, back predictive, tablet (orientation ignore).

---

## 8. Rollback

```
git revert <hash>
# hoặc về mốc gốc
git checkout pre-upgrade-baseline
```

Rollback target-only: `targetSdk = 36` giữ `compileSdk = 37` nếu Play/large-screen blocker.

---

## 9. Kết luận

Upgrade **đạt compileSdk=37 + targetSdk=37**, build/test xanh, lint error không tăng (giảm 2→1), Kotlin warning 0, native gif `.so` hết, permission set không nở. APK/dex tăng do Ads 25 + AndroidX — đúng kỳ vọng. UI XML không đụng. Nợ có chủ đích: XML lint, Loader/AsyncTask, jetifier, portrait trên màn lớn.
