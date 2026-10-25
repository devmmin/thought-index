import { defineConfig, minimal2023Preset } from '@vite-pwa/assets-generator/config'

// public/icon.svg 하나로 홈 화면 아이콘·파비콘을 전부 만든다: npx pwa-assets-generator
export default defineConfig({
  preset: {
    ...minimal2023Preset,
    maskable: { ...minimal2023Preset.maskable, resizeOptions: { background: '#FBF8F1' } },
    apple: { ...minimal2023Preset.apple, resizeOptions: { background: '#FBF8F1' } },
  },
  images: ['public/icon.svg'],
})
