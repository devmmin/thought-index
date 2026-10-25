<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

/** 구글 로그인 후 서버가 /auth/callback#token=... 으로 보내준다. 토큰을 저장하고 홈으로 */
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

onMounted(async () => {
  const token = new URLSearchParams(window.location.hash.slice(1)).get('token')
  // 주소창과 방문 기록에서 토큰을 지운다
  history.replaceState(null, '', window.location.pathname)
  if (!token) {
    toast.show('구글 로그인에 실패했어요.')
    return router.replace({ name: 'login' })
  }
  auth.setToken(token)
  try {
    await auth.fetchMe()
    router.replace({ name: 'home' })
  } catch (e) {
    toast.show(e.message)
    router.replace({ name: 'login' })
  }
})
</script>

<template>
  <div class="callback">서랍 여는 중…</div>
</template>

<style scoped>
.callback {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-muted);
  font-size: 14px;
}
</style>
