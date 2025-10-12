// vue.config.js
const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  devServer: {
    proxy: {
      // 如果你的 API 請求路徑是以 /api 開頭，例如 /api/users, /api/products
      // 代理就會捕捉到這些請求
      '/api': {
        target: 'http://localhost:8080', // 這裡指向你 Spring Boot 後端的地址
        changeOrigin: true, // 允許跨域
      }
    }
  }
});