# --- Dev stage ---
FROM node:18-alpine AS dev
WORKDIR /app
# 常見開發埠：Vite(5173) / Vue CLI(8080)
EXPOSE 5173 8080
# 使用 --host 讓容器外可以存取；依序嘗試 dev/serve/start（允許以 DEV_HOST 覆寫，預設 0.0.0.0）
CMD ["sh", "-c", "npm ci --no-audit --no-fund && (npm run -s dev -- --host ${DEV_HOST:-0.0.0.0} || npm run -s serve -- --host ${DEV_HOST:-0.0.0.0} || npm run -s start -- --host ${DEV_HOST:-0.0.0.0})"]

# --- Build stage ---
FROM node:18-alpine AS build
WORKDIR /app
# 先安裝依賴，加速快取
COPY package*.json ./
RUN npm ci --no-audit --no-fund
# 複製原始碼並建置
COPY . .
# 移除 source map，避免洩漏原始碼與檔案路徑
RUN npm run build && find dist -type f -name "*.map" -delete

# --- Runtime stage ---
FROM nginx:1.27-alpine
# 複製打包結果
COPY --from=build /app/dist /usr/share/nginx/html
# 設定 Nginx（SPA 路由 fallback）
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]