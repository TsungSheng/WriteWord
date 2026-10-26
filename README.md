# 筆順王 · Android

原生 Java / Android Canvas 的正體國字筆順練習 App。介面依參考圖採淡色底、綠色主色、字形輪廓與十字格；支援觸控筆與手指逐筆描寫。

## 已實作

- 輸入最多 20 個國字並選字練習（不會自動把簡體轉成正體）。
- 起點圓點、方向箭頭、筆畫提示、進度與錯誤回饋。
- 依序檢查起終點、路徑形狀、方向與長度；通過後顯示完整筆畫。
- 逐筆示範、重寫、提示開關、系統中文 TTS 朗讀。
- 本機收藏、已查字快取；內附「永一人大木中文學習髮」十字，可直接離線練習。
- 教育部官方網站入口、資料來源說明。旋轉畫面保留目前字，但重新開始筆畫進度。

## 在 Android Studio 執行

1. 安裝 Android Studio，準備 JDK 17、Android SDK Platform 35 與對應 Build Tools。
2. 開啟此資料夾，讓 Android Studio 同步 Gradle。專案採 AGP 8.7.3、Gradle 8.9。
3. 若 IDE 要求 Gradle 路徑，先於 PowerShell 執行 `./build.ps1`；腳本會下載 Gradle 8.9 並驗證官方 SHA256。亦可自行安裝 Gradle 8.9。
4. 設定 `ANDROID_HOME`，或讓 Android Studio 產生 `local.properties`（`sdk.dir`）。連接 Android 8.0 以上手機／平板，按 Run。

PowerShell 建置：`./build.ps1`；單元測試：`./build.ps1 -Test`。

APK 路徑：`app/build/outputs/apk/debug/app-debug.apk`。GitHub Actions 亦提供測試與 debug APK 建置工作流；需自行將專案推送至 GitHub 才會執行。

## 資料與授權

教育部網站：https://stroke-order.learningweb.moe.edu.tw/

其版權說明：https://stroke-order.learningweb.moe.edu.tw/page.jsp?ID=52 ，筆順動畫與全筆順提示採 CC 姓名標示－非商業性－禁止改作 3.0 台灣版。本專案沒有重製或轉換該網站的筆順素材；教育部按鈕在外部瀏覽器開啟官方網站。若要將教育部標準筆順轉換成互動評分資料，須先釐清或取得適用授權。

目前練習資料使用 Hanzi Writer Data **2.0.1**（Make Me a Hanzi／文鼎字型），依 Arphic Public License 使用。完整授權位於 `app/src/main/assets/licenses/ARPHICPL.TXT`。上游：https://github.com/chanind/hanzi-writer-data 。JSON 檔保持原樣，未修改字形資料。

**本版不能宣稱符合教育部標準筆順。** 開放資料含正體字，但部分字形和順序與台灣教育部標準不同；尚未進行逐字校對。正式教育產品若以教育部標準為驗收條件，仍需取得適當資料授權並替換資料層。

首次下載其他字時使用 jsDelivr HTTPS，該字會送往 CDN；手寫軌跡不會上傳。無帳號、廣告或分析 SDK。收藏和快取只保存在 App 本機儲存空間，移除 App 後清除。

## 驗證狀態與限制

已檢查內附十個資料檔的筆畫／中線結構，並提供方向、錯誤形狀、點擊與不均勻採樣的 StrokeMatcher 單元測試。開發環境未找到 JDK 或 Android SDK，**尚未執行 Android 編譯、JUnit、模擬器與實機 UI 測試，也尚未產出 APK**。

判定為幾何容差比對，不是完整的手寫辨識；目前無壓感、掌觸排除、筆畫中途旋轉續寫、逐筆語音教學或已完成學習紀錄頁。示範為逐筆顯示，尚未沿單筆路徑漸進播放。

實機驗收建議：飛航模式練習「永」、反向描橫畫應拒絕、正向描寫應前進、連續查兩個字不能顯示舊請求、收藏後重開 App 仍可選取、示範中按重寫會中止示範，並測試平板橫直向與系統字體放大。
