import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': resolve(import.meta.dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // 默认打开发起的 IDE 后端；需要指向另一端口（例如另跑一个 jar 实例做验收）时
      // 用 VITE_API_TARGET=http://localhost:8082 启动 dev server，不改这份配置。
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/uploads': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
