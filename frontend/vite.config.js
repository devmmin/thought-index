import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    // 개발 중엔 /api 요청을 스프링(8080)으로 넘긴다 → 브라우저 입장에선 같은 출처라 CORS 설정이 필요 없다
    port: 5190,
    proxy: { '/api': 'http://localhost:8080' },
  },
})
