# 筆順王 · Android

原生 Java / Android Canvas 的正體國字筆順練習 App。介面依參考圖採淡色底、綠色主色、字形輪廓與十字格；支援觸控筆與手指逐筆描寫。

## 已實作

- 輸入最多 20 個國字並選字練習（不會自動把簡體轉成正體）。
- 起點圓點、方向箭頭、筆畫提示、進度與錯誤回饋。
- 依序檢查起終點、路徑形狀、方向與長度；通過後顯示完整筆畫。
- 逐筆示範、重寫、提示開關、系統中文 TTS 朗讀。
- **v0.2.0**：寫對每一筆後自動朗讀「點、橫、撇、捺」等筆畫名稱，示範也有逐筆語音。換字、重寫、重新示範與離開 App 會停止目前語音。
- 「全筆順」按鈕可打開／隱藏右側可捲動的累積字形列表，每格增加一筆，新增筆畫為綠色。完成進度會高亮並捲動跟隨；面板的 × 也能關閉，開關狀態會保存。
- 本機收藏、已查字快取；內附「永一人大木中文學習髮」十字，可直接離線練習。
- 教育部官方網站入口、資料來源說明。旋轉畫面保留目前字，但重新開始筆畫進度。

## 在 Android Studio 執行

1. 安裝 Android Studio，準備 JDK 17、Android SDK Platform 35 與對應 Build Tools。
2. 開啟此資料夾，讓 Android Studio 同步 Gradle。專案採 AGP 8.7.3、Gradle 8.9。
3. 若 IDE 要求 Gradle 路徑，先於 PowerShell 執行 `./build.ps1`；腳本會下載 Gradle 8.9 並驗證官方 SHA256。亦可自行安裝 Gradle 8.9。
4. 設定 `ANDROID_HOME`，或讓 Android Studio 產生 `local.properties`（`sdk.dir`）。連接 Android 8.0 以上手機／平板，按 Run。

PowerShell 建置：`./build.ps1`；單元測試：`./build.ps1 -Test`。

APK 路徑：`app/build/outputs/apk/debug/app-debug.apk`。GitHub Actions 亦提供測試與 debug APK 建置工作流；需自行將專案推送至 GitHub 才會執行。

新版 APK 可直接覆蓋安裝舊版，不必先移除 App，可保留收藏及快取（需使用相同 debug 簽章）。

## 資料與授權

教育部網站：https://stroke-order.learningweb.moe.edu.tw/

其版權說明：https://stroke-order.learningweb.moe.edu.tw/page.jsp?ID=52 ，筆順動畫與全筆順提示採 CC 姓名標示－非商業性－禁止改作 3.0 台灣版。本專案沒有重製或轉換該網站的筆順素材；教育部按鈕在外部瀏覽器開啟官方網站。若要將教育部標準筆順轉換成互動評分資料，須先釐清或取得適用授權。

目前練習資料使用 Hanzi Writer Data **2.0.1**（Make Me a Hanzi／文鼎字型），依 Arphic Public License 使用。完整授權位於 `app/src/main/assets/licenses/ARPHICPL.TXT`。上游：https://github.com/chanind/hanzi-writer-data 。JSON 檔保持原樣，未修改字形資料。

v0.2.0 的筆畫名稱使用 **cnchar / cnchar-order / cnchar-trad 3.2.6**（MIT；https://github.com/theajack/cnchar），從該套件匯出 9,504 字，轉為正體名稱，另人工補上本 App「髮」字對應的 15 筆，共 9,505 字。授權附於 `app/src/main/assets/licenses/CNCHAR-MIT.txt`。名稱資料是參考資料，沒有逐字驗證與字形來源的順序完全一致；只有筆畫數一致才按索引採用，缺少資料或數量不符時，畫面顯示並朗讀「第幾筆」，不憑空猜測名稱。部分上游共用的名稱代碼會依中線末段區分橫撇／橫鉤和斜鉤／臥鉤；其他別名採主要名稱。

`scripts/generate-stroke-names.cjs` 為匯出腳本（使用 `.tools/cnchar` 下三個 npm 套件的 package 資料夾）；`stroke-name-overrides.json` 記錄人工修訂。`node scripts/check-stroke-names.cjs` 可檢查字典結構與十個內附字的名稱數量。

**本版不能宣稱符合教育部標準筆順。** 開放資料含正體字，但部分字形和順序與台灣教育部標準不同；尚未進行逐字校對。正式教育產品若以教育部標準為驗收條件，仍需取得適當資料授權並替換資料層。

首次下載其他字時使用 jsDelivr HTTPS，該字會送往 CDN；手寫軌跡不會上傳。無帳號、廣告或分析 SDK。收藏和快取只保存在 App 本機儲存空間，移除 App 後清除。

## 驗證狀態與限制

2026-10-26 已在 Windows 本機以 Java 17、Gradle 8.9、Android SDK Platform 35 成功執行 `assembleDebug`，產出 `app/build/outputs/apk/debug/app-debug.apk`。本機 SDK 路徑為 `E:/Android_SDK`，記錄於不納入版本控制的 `local.properties`。`build.ps1` 會優先使用 `.tools/jdk17` 內的 Java 17；Android Studio 內附的 Java 25 不適用於目前 Gradle 版本，IDE 的 Gradle JDK 請選 Java 17。

已檢查十個內附資料檔的筆畫／中線結構。v0.2.0 已成功編譯；八個 StrokeMatcher／StrokeNames 測試以 JUnitCore 直接執行全部通過，並通過名稱字典檢查。Gradle 的 `testDebugUnitTest` 在本機中文路徑仍有 `ClassNotFoundException` 載入問題，尚未修正；不代表該 Gradle 測試任務已通過。使用者已於舊版確認手機中文朗讀可用，v0.2.0 的逐筆語音與面板尚待實機驗證。

判定為幾何容差比對，不是完整的手寫辨識；目前無壓感、掌觸排除、筆畫中途旋轉續寫、逐筆語音教學或已完成學習紀錄頁。示範為逐筆顯示，尚未沿單筆路徑漸進播放。

實機驗收建議：飛航模式練習「永」、反向描橫畫應拒絕、正向描寫應前進、連續查兩個字不能顯示舊請求、收藏後重開 App 仍可選取、示範中按重寫會中止示範，並測試平板橫直向與系統字體放大。
