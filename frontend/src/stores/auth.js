import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'
import { setAuthHandlers } from '@/api/client'
import { useTabsStore } from '@/stores/tabs'

const TOKEN_KEY = 'thought-index.token'

/** 사파리 비공개 모드 등에서 localStorage 가 막혀도 앱이 죽지 않게 */
const storage = {
  get: () => {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set: (v) => {
    try {
      v ? localStorage.setItem(TOKEN_KEY, v) : localStorage.removeItem(TOKEN_KEY)
    } catch {
      /* 저장 못 해도 이번 탭에서는 메모리 값으로 동작 */
    }
  },
}

/**
 * 토큰은 localStorage 에 둔다.
 * 쿠키(httpOnly)가 XSS 에는 더 안전하지만, 화면(Vercel)과 서버(Render)의 도메인이 달라
 * 서드파티 쿠키 차단에 걸린다. 개인 메모 앱 규모에서는 헤더 토큰이 단순하고 확실하다.
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(storage.get())
  const member = ref(null)
  const isLoggedIn = computed(() => !!token.value)

  function setToken(value) {
    token.value = value
    storage.set(value)
  }

  async function login(payload) {
    const res = await authApi.login(payload)
    setToken(res.accessToken)
    member.value = res.member
  }

  async function signup(payload) {
    const res = await authApi.signup(payload)
    setToken(res.accessToken)
    member.value = res.member
  }

  async function fetchMe() {
    if (!token.value) return null
    member.value = await authApi.me()
    return member.value
  }

  function logout() {
    setToken(null)
    member.value = null
    useTabsStore().reset()
  }

  return { token, member, isLoggedIn, setToken, login, signup, fetchMe, logout }
})

/** main.js 에서 한 번 호출: client 가 토큰을 쓰고, 401 이면 로그아웃 후 로그인 화면으로 */
export function installAuth(pinia, router) {
  const store = useAuthStore(pinia)
  setAuthHandlers({
    getToken: () => store.token,
    onUnauthorized: () => {
      store.logout()
      if (router.currentRoute.value.meta.public) return
      router.replace({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    },
  })
}
