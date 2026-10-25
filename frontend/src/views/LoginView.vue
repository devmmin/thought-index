<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { API_ORIGIN } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const toast = useToast()

const mode = ref('login') // login | signup
const email = ref('')
const password = ref('')
const name = ref('')
const error = ref('')
const submitting = ref(false)
const googleEnabled = ref(false)

const isSignup = computed(() => mode.value === 'signup')
// 구글 로그인은 화면이 아니라 서버 주소로 이동해야 시작된다 (서버가 구글로 보냈다가 다시 /auth/callback 으로 돌려보냄)
const googleUrl = `${API_ORIGIN}/oauth2/authorization/google`

onMounted(async () => {
  if (route.query.error === 'google') toast.show('구글 로그인에 실패했어요.')
  try {
    googleEnabled.value = (await authApi.providers()).google
  } catch {
    googleEnabled.value = false
  }
})

function switchMode() {
  mode.value = isSignup.value ? 'login' : 'signup'
  error.value = ''
}

async function submit() {
  error.value = ''
  if (!email.value.trim() || !password.value) {
    error.value = '이메일과 비밀번호를 적어 주세요.'
    return
  }
  if (isSignup.value && password.value.length < 8) {
    error.value = '비밀번호는 8자 이상이에요.'
    return
  }
  submitting.value = true
  try {
    const payload = { email: email.value.trim(), password: password.value }
    if (isSignup.value) await auth.signup({ ...payload, name: name.value.trim() || undefined })
    else await auth.login(payload)
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (e) {
    error.value = e.fieldErrors?.[0]?.reason ?? e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login">
    <!-- 위에서 늘어진 인덱스 스티커 -->
    <div class="stickers" aria-hidden="true">
      <span style="background: #ffd6a5; height: 76px" />
      <span style="background: #cdeac0; height: 58px" />
      <span style="background: #bfd7f5; height: 68px" />
      <span style="background: #e3ccf2; height: 50px" />
    </div>

    <h1>생각서랍</h1>
    <p class="sub">흩어진 생각을 인덱스로 정리해요</p>

    <form class="form" @submit.prevent="submit">
      <template v-if="googleEnabled">
        <a class="btn google" :href="googleUrl">
          <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
            <path fill="#EA4335" d="M24 9.5c3.5 0 6.6 1.2 9 3.5l6.7-6.7C35.6 2.4 30.2 0 24 0 14.6 0 6.6 5.4 2.6 13.3l7.8 6C12.3 13.6 17.6 9.5 24 9.5z" />
            <path fill="#4285F4" d="M46.1 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.4c-.5 2.9-2.2 5.3-4.6 7l7.2 5.6c4.2-3.9 7.1-9.7 7.1-17.1z" />
            <path fill="#FBBC05" d="M10.4 28.7c-.5-1.4-.8-3-.8-4.7s.3-3.2.8-4.7l-7.8-6C.9 16.6 0 20.2 0 24s.9 7.4 2.6 10.7l7.8-6z" />
            <path fill="#34A853" d="M24 48c6.5 0 11.9-2.1 15.9-5.8l-7.2-5.6c-2 1.4-4.6 2.2-8.7 2.2-6.4 0-11.7-4.1-13.6-9.8l-7.8 6C6.6 42.6 14.6 48 24 48z" />
          </svg>
          Google로 계속하기
        </a>
        <div class="or">또는 이메일로</div>
      </template>

      <input v-if="isSignup" v-model="name" maxlength="30" placeholder="이름 (선택)" autocomplete="nickname" aria-label="이름" />
      <input v-model="email" type="email" placeholder="name@example.com" autocomplete="email" aria-label="이메일" @input="error = ''" />
      <input
        v-model="password"
        type="password"
        :placeholder="isSignup ? '비밀번호 (8자 이상)' : '비밀번호'"
        :autocomplete="isSignup ? 'new-password' : 'current-password'"
        aria-label="비밀번호"
        @input="error = ''"
      />
      <p v-if="error" class="error" role="alert">{{ error }}</p>

      <button type="submit" class="btn primary" :disabled="submitting">
        {{ submitting ? '여는 중…' : isSignup ? '서랍 만들기' : '로그인' }}
      </button>

      <p class="switch">
        {{ isSignup ? '이미 서랍이 있어요?' : '처음이에요?' }}
        <button type="button" @click="switchMode">{{ isSignup ? '로그인' : '가입하기' }}</button>
      </p>
    </form>
  </div>
</template>

<style scoped>
.login {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 24px calc(28px + env(safe-area-inset-bottom));
}

.stickers {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.stickers span {
  width: 36px;
  border-radius: 0 0 12px 12px;
}

h1 {
  margin: 36px 0 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.04em;
}
.sub {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--ink-muted);
}

.form {
  margin-top: auto;
  padding-top: 40px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

input {
  height: 48px;
  padding: 0 16px;
  border-radius: 14px;
  border: 1px solid var(--card-border);
  background: var(--card);
  font-size: 16px;
  outline: none;
}
input:focus {
  border-color: var(--ink-muted);
}
::placeholder {
  color: #c4bdab;
}

.btn {
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
}
.primary {
  background: #e8954a;
  color: #fff;
}
.primary:disabled {
  opacity: 0.7;
}
.google {
  background: var(--card);
  border: 1px solid #e1dac8;
  color: var(--ink);
}

.or {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--ink-muted);
}
.or::before,
.or::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--paper-line);
}

.error {
  margin: -4px 0 0;
  font-size: 13px;
  color: var(--danger);
}

.switch {
  margin: 4px 0 0;
  text-align: center;
  font-size: 14px;
  color: var(--ink-soft);
}
.switch button {
  color: #c27a2c;
  font-weight: 600;
}
</style>
