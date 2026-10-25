import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    vue(),
    // PWA: 휴대폰 "홈 화면에 추가" + 화면 파일 캐시. 데이터(/api)는 캐시하지 않는다 (항상 최신 메모)
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'apple-touch-icon-180x180.png', 'icon.svg'],
      manifest: {
        name: '생각서랍',
        short_name: '생각서랍',
        description: '생각을 인덱스 탭에 꽂아 정리하는 메모',
        lang: 'ko',
        theme_color: '#FBF8F1',
        background_color: '#FBF8F1',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: 'pwa-64x64.png', sizes: '64x64', type: 'image/png' },
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
          { src: 'maskable-icon-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        navigateFallback: '/index.html',
        // 이 주소들은 화면이 아니라 서버로 가야 한다
        navigateFallbackDenylist: [/^\/api\//, /^\/oauth2\//, /^\/login\/oauth2\//],
      },
    }),
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5190,
    // 개발 중엔 /api 요청을 스프링(8080)으로 넘긴다 → 브라우저 입장에선 같은 출처라 CORS 설정이 필요 없다
    proxy: { '/api': 'http://localhost:8080' },
  },
})
