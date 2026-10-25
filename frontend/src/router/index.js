import { createRouter, createWebHistory } from 'vue-router'
import NotebookView from '@/views/NotebookView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: NotebookView },
    { path: '/tabs/:tabId', name: 'tab', component: NotebookView, props: (r) => ({ tabId: Number(r.params.tabId) }) },
    // 새 메모(/notes/new)와 기존 메모(/notes/6)를 "같은 라우트"로 둔다.
    // 첫 저장 후 주소를 /notes/6 으로 바꿔도 화면이 다시 만들어지지 않아서 쓰던 글이 끊기지 않는다.
    {
      path: '/notes/:noteId(new|\\d+)',
      name: 'note',
      component: () => import('@/views/NoteEditorView.vue'),
      props: (r) => ({
        noteId: r.params.noteId === 'new' ? null : Number(r.params.noteId),
        initialTabId: Number(r.query.tabId) || null,
      }),
    },
    { path: '/search', name: 'search', component: () => import('@/views/SearchView.vue') },
    // 로그인 없이 볼 수 있는 화면은 meta.public
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/auth/callback',
      name: 'auth-callback',
      component: () => import('@/views/AuthCallbackView.vue'),
      meta: { public: true },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// 로그인 안 했으면 로그인 화면으로, 로그인했는데 로그인 화면이면 홈으로
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) {
    return { name: 'login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : undefined }
  }
  if (to.name === 'login' && auth.isLoggedIn) return { name: 'home' }
})

export default router
