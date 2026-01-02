## 流程品質提升措施 (Process Quality Improvement Measures)

本專案導入了現代化雲端部署流程與版本控制策略，以降低人為操作錯誤並提升開發效率。

### 1. 雲端自動化部署 (Automated Cloud Deployment)
* **Render 自動觸發部署 (Auto-Deploy)**：
    * 本專案將前後端分別託管於 Render 雲端平台。
    * 系統已設定與 GitHub Repository 連動，當開發者 Push 程式碼至 `main` 分支時，Render 會自動偵測變更，並立即觸發 Build (建置) 與 Deploy (部署) 流程。
    * **改善成效**：實現了 CI/CD (持續整合/持續部署) 的核心概念，確保雲端環境永遠保持在最新的穩定版本，無須人工手動上傳檔案。

### 2. Git 分支管理策略 (Git Branching Strategy)
* **分支隔離開發**：
    * 團隊採用 `Feature Branch` 開發模式（例如 `google-oauth` 分支），開發新功能時不直接影響主線。
    * 透過 Pull Request (PR) 進行程式碼合併，確保功能完成且無衝突後才合併至 `main` 分支。
* **版本控制透明化**：
    * 詳細的 Commit Message 紀錄（如 `fix:`, `feat:`, `chore:`），有助於團隊追蹤問題與回溯版本。

---

## 其他有助於說明專案優點或貢獻的說明

### 1. 系統架構優勢 (System Architecture)
* **前後端分離設計 (Decoupled Architecture)**：
    * **前端**：使用 **Vue.js** 框架，提供流暢的單頁應用 (SPA) 體驗，畫面更新不需重新載入整頁。
    * **後端**：使用 **Spring Boot** 建構 RESTful API，專注於業務邏輯與資料處理，具備高擴充性。
    * **資料庫**：採用 **MongoDB Atlas** 雲端資料庫，適應彈性的菜單結構與訂單資料。

### 2. 安全性提升 (Security Enhancements)
* **OAuth 2.0 第三方登入整合**：
    * 實作 **Google** 與 **Facebook** 登入功能，降低使用者註冊門檻，並避免將使用者密碼以明文方式儲存在自家資料庫，提升帳戶安全性。
* **全站 HTTPS 加密傳輸**：
    * 透過 Render 提供的 SSL 憑證，強制使用 HTTPS 協定，保護使用者在傳輸過程中的資料（如個人資訊、訂單內容）不被竊取。

### 3. 使用者體驗優化 (UX Optimization)
* **錯誤頁面處理 (Error Handling)**：
    * 針對 404 或未預期的錯誤進行路由重寫 (Rewrite Rules)，引導使用者回到正確頁面，避免出現服務中斷的困惑。
* **響應式設計**：
    * 前端介面適配不同裝置，無論是手機或電腦皆能順暢點餐。
