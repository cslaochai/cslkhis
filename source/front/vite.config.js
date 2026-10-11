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
    host: true,
    // 内网穿透联调（花生壳）：壳域名走隧道访问 dev server（页面 + /api 回调透传）。
    // Vite 5.4.12+ 默认只放行 localhost，公网域名 Host 会被拦（Blocked request），这里放行贝锐壳域名子域。
    allowedHosts: ['.oray.net', '.vicp.fun', '.vicp.cc', '.oicp.net'],
    proxy: {
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
