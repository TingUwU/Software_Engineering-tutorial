## 需求文件：https://hackmd.io/@Software-Engineering-team5/SysAi3Upge
## 設計文件：https://hackmd.io/@Software-Engineering-team5/rk3ua3-WWl
## 測試文件：https://hackmd.io/@Software-Engineering-team5/r1JSXlmQ-e
## Demo 影片：https://www.youtube.com/watch?v=6uuuUWWm84k&feature=youtu.be
## Trello：https://trello.com/b/i1pOlvNH/my-trello-board


## Git repo：https://github.com/TingUwU/Software_Engineering-tutorial

## 流程品質提升措施 (Process Quality Improvement Measures)

本專案導入現代化開發流程、容器化技術與嚴謹的架構設計，以降低人為錯誤、提升協作效率並確保系統穩定性。

### 1. 雲端自動化部署 (Automated Cloud Deployment / CI/CD)
* **Render 自動觸發部署 (Auto-Deploy)**：
    * 本專案將前後端分別託管於 Render 雲端平台，並設定與 GitHub Repository 連動。
    * 當開發者 Push 程式碼至 `main` 分支時，Render 會自動偵測變更，立即觸發 Build (建置) 與 Deploy (部署) 流程。
    * **成效**：實現 CI/CD (持續整合/持續部署) 核心概念，確保雲端環境恆常保持在最新的穩定版本，免去人工手動上傳檔案的風險與時間成本。

### 2. 環境一致性與容器化 (Environment Consistency & Dockerization)
* **標準化運行環境**：
    * 利用 `Dockerfile` 定義專案的運行環境與依賴項目。
    * **成效**：確保「開發環境 (Development)」與「生產環境 (Production)」的高度一致性，解決 "It works on my machine" 的常見部署問題；同時支援跨平台部署，不侷限於本地開發環境。

### 3. Git 分支管理策略 (Git Branching Strategy)
* **分支隔離開發**：
    * 團隊採用 `Feature Branch` 開發模式（例如 `google-oauth` 分支），開發新功能時不直接影響主線。透過 Pull Request (PR) 進行代碼審查，確保無衝突後才合併至 `main` 分支。
* **版本控制透明化**：
    * 落實詳細的 Commit Message 規範（如 `fix:`, `feat:`, `chore:`），有助於團隊追蹤問題與回溯版本。

### 4. 軟體架構與組態管理 (Software Architecture & Configuration)
* **分層架構設計 (Layered Architecture)**：
    * 後端採用嚴謹的 **Controller - Service - Repository** 三層式架構，明確分離職責，提升程式碼的可讀性與維護性。
* **安全性與組態管理**：
    * 敏感資訊（如資料庫連線字串、API Keys）不寫死在程式碼中，而是利用雲端平台 (Render) 的 **Environment Variables (環境變數)** 功能進行管理，大幅提升系統安全性。

---

## 其他有助於說明專案優點或貢獻的說明

### 1. 前後端分離與 API 優先策略 (API-First & Decoupled Architecture)
* **API 優先 (API First)**：
    * 遵循 RESTful API 設計原則，後端提供標準化的 JSON 資料介面。
    * **成效**：開發效率高，前後端團隊可並行開發；且介面標準化使得系統易於與第三方服務整合或進行擴充。
* **技術選型優勢**：
    * **前端**：使用 **Vue.js** 框架，提供流暢的單頁應用 (SPA) 體驗，畫面更新不需重新載入整頁。
    * **後端**：使用 **Spring Boot** 建構，專注於業務邏輯與資料處理，具備高擴充性與成熟的生態系支援。
    * **資料庫**：採用 **MongoDB Atlas** 雲端資料庫，NoSQL 的特性適應彈性的菜單結構與多變的訂單資料。

### 2. 安全性提升 (Security Enhancements)
* **OAuth 2.0 第三方登入整合**：
    * 實作 **Google** 與 **Facebook** 登入功能，降低使用者註冊門檻，並避免將使用者密碼以明文方式儲存在自家資料庫，有效降低資安風險。
* **全站 HTTPS 加密傳輸**：
    * 透過 Render 提供的 SSL 憑證，強制使用 HTTPS 協定，保護使用者在傳輸過程中的資料（如個人資訊、訂單內容）不被中間人竊取。

### 3. 使用者體驗優化 (UX Optimization)
* **錯誤頁面處理 (Error Handling)**：
    * 針對 404 或未預期的錯誤進行路由重寫 (Rewrite Rules)，引導使用者回到正確頁面（如首頁），避免出現服務中斷的困惑感。
* **響應式設計 (Responsive Design)**：
    * 前端介面針對不同螢幕尺寸進行適配，無論是手機、平板或電腦，使用者皆能順暢地瀏覽菜單與進行點餐。