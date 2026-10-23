import { createRouter, createWebHistory } from 'vue-router'
import NotebookView from '@/views/NotebookView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: NotebookView },
    { path: '/tabs/:tabId', name: 'tab', component: NotebookView, props: (r) => ({ tabId: Number(r.params.tabId) }) },
    // 편집·검색 화면은 처음 들어갈 때 불러온다 (코드 분할)
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
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})
