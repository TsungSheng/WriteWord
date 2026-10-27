# 筆順王 · Android

目前版本：**1.1.1**（versionCode 10）。Release APK 檔名固定為 `WriteOrder.apk`，沿用原 release 簽署金鑰，可直接覆蓋先前 release 版。

原生 Java / Android Canvas 的正體國字筆順練習 App。介面依參考圖採淡色底、綠色主色、字形輪廓與十字格；支援觸控筆與手指逐筆描寫。

## 已實作

**v1.1.1**：多字練習時，在主要注音上方新增「▲ 上一字」、下方新增「下一字 ▼」原生按鈕，均使用 48dp 觸控區與無障礙描述。單字模式隱藏按鈕，第一字停用上一字、最後一字停用下一字，不首尾循環。尚未完成目前字也可跳字；跳字會停止語音、取消待執行的自動換字與示範，並從所選字第一筆重練，重複國字仍依其在練習組中的位置移動。注音側欄沿用原本寬度，不再額外縮小大字寬度；按鈕區也不會被當成手寫筆畫。

本版通過 19 項 Gradle 單元測試（新增未完成跳字、重複字位置、跳字後完成狀態清除及首尾邊界案例）、release 建置與簽章驗證。按鈕的手機實際位置與觸控仍待實機確認。

**v1.1.0**：第一排操作鈕改為「全筆順、重寫、示範、提示、朗讀」；收藏、教育部與說明留在第二排。主畫面、練習格、筆順小圖與對話框改用暗色系，完成筆畫以淺綠色、方向提示以淺紫色顯示。

多字輸入改為依輸入順序逐字練習，不再先跳出選字視窗。保留重複國字、略過非漢字，最多 20 字；畫面顯示「第幾字／共幾字」。完成目前字後，先朗讀最後一筆及國字，再短暫停留並切換下一字；沒有可用語音時約 1.2 秒後切換，語音引擎未回報時有 15 秒保底等待。整組完成後停在最後一字，不循環。示範不會跳字，重寫只重練目前字，重新輸入並開始或選收藏字會建立新練習組。

自動切換等待期間重寫、重新示範或開始新練習，會取消舊切換；App 進入背景時停止語音並暫停切換，回到前景才繼續。旋轉畫面保留練習組與目前字的位置，但目前字的筆畫仍會重新開始。若某字資料無法下載，會停在該字顯示錯誤，不會把它算作完成或自動略過。

v1.1.0 已通過 16 項 Gradle 單元測試（含多字順序、重複字、完成判定、換組、重寫、擴充漢字、無效輸入、復原位置與 20 字上限），release 建置、必要 lint、簽章驗證、筆畫名稱字典及 22 項注音轉換檢查。暗色 UI 與語音結束後自動換字的整合流程仍待手機實測。

**v0.3.3**：移除主畫面底部常駐的兩行資料來源／提示文字，改為下方既有按鈕列的「說明」按鈕，點擊後顯示原有資料來源、授權與使用說明。底部五個按鈕平均分配寬度，不另增加按鈕列，釋出高度給練習區。

**v0.3.2**：全筆順面板改放在大字下方，使用固定高度的橫向累積字形列，可左右滑動。大字練習區保留完整寬度，筆順面板不再占用右側寬度；進度高亮與自動跟隨捲動改為橫向，仍可按「全筆順／隱藏筆順」或右側 × 開關。

**v0.3.1**：修正「瓊」的筆畫名稱資料。其練習字形為 19 筆，而 cnchar 的名稱只有 18 筆，原先會觸發全部改唸筆次的保護機制；現依目前字形逐筆補齊 19 個名稱，第 17 筆為「橫」，並加入回歸檢查。部首修訂為「玉（王）」。增加「瓊」離線字形，目前共 12 字。保留缺少名稱或筆數不一致時的備援機制，這類情況與輸入第幾個字無關。教育部參考：https://dict.concised.moe.edu.tw/dictView.jsp?ID=26200&la=0&powerMode=0 。

**v0.3.0**：移除頂部標語與「逐筆練習」標題，輸入列直接置頂；保留高密度手機的輸入高度修正，鍵盤搜尋鍵可直接開始，開始後收起鍵盤。大字右側直排主要注音，其他讀音於上方換行列出；新增部首與總筆畫（依實際練習字形計數）。寫完整個字後，在最後一筆名稱語音之後自動朗讀國字，示範結束也會朗讀。App 圖示改為綠底白色「陳」，並內附「陳」字離線資料，共 11 字。

v0.2.1 修正高密度手機的輸入欄文字被裁切：移除原本固定 56 實體像素的高度，改成隨內容調整並至少 56dp，增加標題與輸入列間距，統一以 dp 處理根容器留白。使用與 v0.2.0 release 相同的簽署金鑰，可直接覆蓋更新。

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

PowerShell debug 建置：`./build.ps1`；release 建置：`./build.ps1 -Release`；單元測試：`./build.ps1 -Test`。

若專案位於 OneDrive 或中文路徑造成檔案鎖定／測試載入問題，可指定不在同步資料夾內、全英文的建置目錄：`./build.ps1 -Release -BuildDirectory C:/AndroidBuild/WriteOrder`。該次產物會輸出至指定目錄的 `outputs/apk/release/WriteOrder.apk`，原始碼仍留在專案。Gradle 對應參數為 `-PwriteOrderBuildRoot=...`。

APK 固定輸出名稱為 `WriteOrder.apk`，依建置類型分別位於 `app/build/outputs/apk/debug/WriteOrder.apk` 與 `app/build/outputs/apk/release/WriteOrder.apk`。GitHub Actions 亦提供測試與 debug APK 建置工作流；需自行將專案推送至 GitHub 才會執行。

新版 APK 可直接覆蓋安裝舊版，不必先移除 App，可保留收藏及快取（需使用相同 debug 簽章）。

## Release 簽署

已產出 v0.3.0 已簽署的 release APK，`debuggable=false`，沿用 v0.2.0 的獨立 RSA 3072 位元金鑰，已通過 `apksigner verify` 與 Gradle release 必要 lint 檢查，可直接覆蓋先前 release 版。Release 保留既有 App 功能，目前未開啟程式碼縮減。

本機金鑰：`.signing/WriteOrder-release.p12`；對應密碼與別名設定：`signing.properties`。**請將兩者一起安全備份**，未來版本須持續使用相同金鑰才能覆蓋更新。這些檔案已排除於 Git，請勿將它們推送至 GitHub。其他電腦建置時，應還原這兩個檔案；`signing.properties.example` 提供格式參考。

`setup-release-signing.ps1` 只用於全新專案首次產生金鑰；偵測到既有金鑰或設定檔時會停止，避免更換原有簽章。金鑰密碼為安全亂數產生，腳本不顯示密碼。未提供 release 簽署設定時，release 建置會明確停止，不會交付未簽署 APK。

Release 與先前的 debug APK 簽章不同，無法直接覆蓋安裝；在同一手機改裝 release 時須先移除舊 debug 版，移除會清除本機收藏與快取。後續以相同 release 金鑰簽署的新版可正常更新。

## 資料與授權

教育部網站：https://stroke-order.learningweb.moe.edu.tw/

其版權說明：https://stroke-order.learningweb.moe.edu.tw/page.jsp?ID=52 ，筆順動畫與全筆順提示採 CC 姓名標示－非商業性－禁止改作 3.0 台灣版。本專案沒有重製或轉換該網站的筆順素材；教育部按鈕在外部瀏覽器開啟官方網站。若要將教育部標準筆順轉換成互動評分資料，須先釐清或取得適用授權。

目前練習資料使用 Hanzi Writer Data **2.0.1**（Make Me a Hanzi／文鼎字型），依 Arphic Public License 使用。完整授權位於 `app/src/main/assets/licenses/ARPHICPL.TXT`。上游：https://github.com/chanind/hanzi-writer-data 。JSON 檔保持原樣，未修改字形資料。

v0.2.0 的筆畫名稱使用 **cnchar / cnchar-order / cnchar-trad 3.2.6**（MIT；https://github.com/theajack/cnchar），從該套件匯出 9,504 字，轉為正體名稱，另人工補上本 App「髮」字對應的 15 筆，共 9,505 字。授權附於 `app/src/main/assets/licenses/CNCHAR-MIT.txt`。名稱資料是參考資料，沒有逐字驗證與字形來源的順序完全一致；只有筆畫數一致才按索引採用，缺少資料或數量不符時，畫面顯示並朗讀「第幾筆」，不憑空猜測名稱。部分上游共用的名稱代碼會依中線末段區分橫撇／橫鉤和斜鉤／臥鉤；其他別名採主要名稱。

`scripts/generate-stroke-names.cjs` 為匯出腳本（使用 `.tools/cnchar` 下三個 npm 套件的 package 資料夾）；`stroke-name-overrides.json` 記錄人工修訂。`node scripts/check-stroke-names.cjs` 可檢查字典結構與十個內附字的名稱數量。

v0.3.0 的離線注音／部首字典 `character-info.json` 含 9,572 字，來自 cnchar / cnchar-trad / cnchar-radical 3.2.6（同 MIT 授權）。`scripts/zhuyin.cjs` 執行拼音至注音及聲調轉換，`scripts/generate-character-info.cjs` 產生字典與原生 vector 圖示；另人工修訂「髮」讀音／部首、「陳」部首及「黃」部首。圖示直接使用「陳」字原始筆畫路徑，完整字形資料同時附於 assets/data，原字形資料未修改。

注音和部首是參考資料，未以臺灣教育部辭典逐字校對，部分讀音可能不同，也可能缺少異讀。語音採手機中文引擎的預設讀音，不保證與多音字畫面中的第一個注音一致；尚無指定異讀的語音選擇功能。字典未收錄時顯示「尚未收錄」，不猜測注音。可用 `node scripts/check-character-info.cjs` 檢查轉換規則、字典及範例字覆蓋。

**本版不能宣稱符合教育部標準筆順。** 開放資料含正體字，但部分字形和順序與台灣教育部標準不同；尚未進行逐字校對。正式教育產品若以教育部標準為驗收條件，仍需取得適當資料授權並替換資料層。

首次下載其他字時使用 jsDelivr HTTPS，該字會送往 CDN；手寫軌跡不會上傳。無帳號、廣告或分析 SDK。收藏和快取只保存在 App 本機儲存空間，移除 App 後清除。

## 驗證狀態與限制

2026-10-26 已在 Windows 本機以 Java 17、Gradle 8.9、Android SDK Platform 35 成功執行 debug 與 release 建置。本機 SDK 路徑為 `E:/Android_SDK`，記錄於不納入版本控制的 `local.properties`。`build.ps1` 會優先使用 `.tools/jdk17` 內的 Java 17；Android Studio 內附的 Java 25 不適用於目前 Gradle 版本，IDE 的 Gradle JDK 請選 Java 17。

已檢查十個內附資料檔的筆畫／中線結構。v0.2.0 已成功編譯；八個 StrokeMatcher／StrokeNames 測試以 JUnitCore 直接執行全部通過，並通過名稱字典檢查。Gradle 的 `testDebugUnitTest` 在本機中文路徑仍有 `ClassNotFoundException` 載入問題，尚未修正；不代表該 Gradle 測試任務已通過。使用者已於舊版確認手機中文朗讀可用，v0.2.0 的逐筆語音與面板尚待實機驗證。

v0.3.0 已在非同步、全英文的建置目錄成功執行 Gradle `testDebugUnitTest`（8 項測試全部通過）與 `assembleRelease`，排除原本中文輸出路徑的類別載入問題；另通過 22 項注音轉換案例、9,572 字元資料結構及全部 11 個內附字的資料覆蓋檢查。新版 UI、注音位置、圖示與完成語音仍待手機實測。

判定為幾何容差比對，不是完整的手寫辨識；目前無壓感、掌觸排除、筆畫中途旋轉續寫、逐筆語音教學或已完成學習紀錄頁。示範為逐筆顯示，尚未沿單筆路徑漸進播放。

實機驗收建議：飛航模式練習「永」、反向描橫畫應拒絕、正向描寫應前進、連續查兩個字不能顯示舊請求、收藏後重開 App 仍可選取、示範中按重寫會中止示範，並測試平板橫直向與系統字體放大。
