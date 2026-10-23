<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { ChevronLeft, Pin, Trash2 } from 'lucide-vue-next'
import { notesApi } from '@/api/notes'
import { paletteOf } from '@/constants/tabColors'
import { useTabsStore } from '@/stores/tabs'
import { useToast } from '@/composables/useToast'
import { formatDate } from '@/composables/formatDate'

const props = defineProps({
  noteId: { type: Number, default: null },
  initialTabId: { type: Number, default: null },
})

const router = useRouter()
const tabsStore = useTabsStore()
const toast = useToast()

const id = ref(props.noteId)
const form = ref({ tabId: props.initialTabId, title: '', content: '', pinned: false })
const updatedAt = ref(null)
const status = ref('idle') // idle | dirty | saving | saved | error
const ready = ref(false)
const confirmingDelete = ref(false)
const contentEl = ref(null)

const palette = computed(() => paletteOf(tabsStore.byId.get(form.value.tabId)?.color))

const statusText = computed(() => {
  if (!form.value.title.trim()) return '제목을 적으면 저장돼요'
  return {
    dirty: '쓰는 중…',
    saving: '저장 중…',
    saved: '저장됨',
    error: '저장 못 했어요',
    idle: updatedAt.value ? formatDate(updatedAt.value) : '',
  }[status.value]
})

onMounted(async () => {
  try {
    await tabsStore.ensureLoaded()
    if (id.value) {
      const note = await notesApi.get(id.value)
      form.value = { tabId: note.tabId, title: note.title, content: note.content, pinned: note.pinned }
      updatedAt.value = note.updatedAt
      lastSavedTabId = note.tabId
    } else if (!form.value.tabId) {
      form.value.tabId = tabsStore.tabs[0]?.id ?? null
    }
    ready.value = true
    await nextTick()
    autoGrow()
  } catch (e) {
    toast.show(e.message)
    router.replace('/')
  }
})

/* ---------- 자동 저장: 입력이 멈추고 0.7초 뒤 ---------- */
let timer = null
let saving = Promise.resolve()
// 서버에 마지막으로 저장된 탭. 다른 탭으로 옮겼을 때 탭 옆 숫자를 맞추려고 기억해 둔다
let lastSavedTabId = null

watch(
  form,
  () => {
    if (!ready.value) return
    status.value = 'dirty'
    clearTimeout(timer)
    timer = setTimeout(save, 700)
  },
  { deep: true },
)

function save() {
  clearTimeout(timer)
  timer = null
  if (!form.value.title.trim() || !form.value.tabId) return saving
  // 저장이 겹치면 새 메모가 두 번 만들어질 수 있어 앞 저장이 끝난 뒤 이어서 한다
  saving = saving.then(async () => {
    status.value = 'saving'
    const payload = { ...form.value }
    try {
      if (id.value) {
        const saved = await notesApi.update(id.value, payload)
        if (lastSavedTabId !== saved.tabId) {
          tabsStore.bumpCount(lastSavedTabId, -1)
          tabsStore.bumpCount(saved.tabId, 1)
        }
        lastSavedTabId = saved.tabId
        updatedAt.value = saved.updatedAt
      } else {
        const saved = await notesApi.create(payload)
        id.value = saved.id
        lastSavedTabId = saved.tabId
        updatedAt.value = saved.updatedAt
        tabsStore.bumpCount(saved.tabId, 1)
        // 주소를 /notes/:id 로 바꿔 둔다 (새로고침해도 이 메모가 열리게)
        router.replace({ name: 'note', params: { noteId: saved.id } })
      }
      status.value = 'saved'
    } catch (e) {
      status.value = 'error'
      toast.show(e.message)
    }
  })
  return saving
}
// 저장 안 된 채로 나가지 않게 마지막 저장을 끝내고 이동
onBeforeRouteLeave(async () => {
  if (timer) await save()
  else await saving
  return true
})

function autoGrow() {
  const el = contentEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${el.scrollHeight}px`
}

function goBack() {
  router.push(form.value.tabId ? `/tabs/${form.value.tabId}` : '/')
}

async function remove() {
  clearTimeout(timer)
  timer = null
  if (!id.value) return router.replace(`/tabs/${form.value.tabId}`)
  try {
    await saving
    await notesApi.remove(id.value)
    tabsStore.bumpCount(form.value.tabId, -1)
    toast.show('메모를 지웠어요')
    ready.value = false
    router.replace(`/tabs/${form.value.tabId}`)
  } catch (e) {
    toast.show(e.message)
  }
}
</script>

<template>
  <div v-if="ready" class="editor" :style="{ '--accent': palette.accent, '--tab': palette.tab, '--ink-tab': palette.ink }">
    <header class="bar">
      <button class="icon" aria-label="뒤로" @click="goBack"><ChevronLeft :size="24" /></button>
      <span class="status">{{ statusText }}</span>
      <button
        class="icon"
        :class="{ on: form.pinned }"
        :aria-label="form.pinned ? '고정 풀기' : '맨 위에 고정'"
        @click="form.pinned = !form.pinned"
      >
        <Pin :size="20" />
      </button>
      <button class="icon" aria-label="지우기" @click="confirmingDelete = !confirmingDelete">
        <Trash2 :size="20" />
      </button>
    </header>

    <Transition name="fold">
      <div v-if="confirmingDelete" class="confirm">
        <span>이 메모를 지울까요?</span>
        <button class="ghost" @click="confirmingDelete = false">그만두기</button>
        <button class="danger" @click="remove">지우기</button>
      </div>
    </Transition>

    <!-- 어느 인덱스에 꽂을지 -->
    <div class="tab-picker" role="radiogroup" aria-label="인덱스">
      <button
        v-for="tab in tabsStore.tabs"
        :key="tab.id"
        role="radio"
        :aria-checked="tab.id === form.tabId"
        class="pick"
        :class="{ active: tab.id === form.tabId }"
        :style="{ background: paletteOf(tab.color).tab, color: paletteOf(tab.color).ink }"
        @click="form.tabId = tab.id"
      >
        {{ tab.name }}
      </button>
    </div>

    <article class="paper">
      <input v-model="form.title" class="title" maxlength="100" placeholder="제목" aria-label="제목" />
      <textarea
        ref="contentEl"
        v-model="form.content"
        class="content"
        maxlength="10000"
        placeholder="떠오르는 대로 적어 보세요"
        aria-label="내용"
        @input="autoGrow"
      />
    </article>
  </div>
</template>

<style scoped>
.editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: calc(8px + env(safe-area-inset-top)) 14px 24px;
  gap: 12px;
}

.bar {
  display: flex;
  align-items: center;
  gap: 4px;
}
.icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: var(--ink-soft);
}
.icon.on {
  color: var(--accent);
}
.status {
  flex: 1;
  font-size: 12px;
  color: var(--ink-muted);
  text-align: center;
}

.confirm {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fbeaea;
  color: #7a3030;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 14px;
}
.confirm span {
  flex: 1;
}
.ghost,
.danger {
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 14px;
}
.danger {
  background: var(--danger);
  color: #fff;
}

.tab-picker {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
  padding: 2px 0;
}
.pick {
  flex-shrink: 0;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 10px 10px 4px 4px;
  opacity: 0.55;
  transition: opacity 0.2s, transform 0.2s var(--ease-soft);
}
.pick.active {
  opacity: 1;
  transform: translateY(-2px);
}

/* 줄 노트: 줄 간격(32px)과 line-height 를 똑같이 맞춰야 글자가 줄 위에 앉는다 */
.paper {
  --line: 32px;
  flex: 1;
  background-color: var(--card);
  background-image: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent calc(var(--line) - 1px),
    var(--paper-line) calc(var(--line) - 1px),
    var(--paper-line) var(--line)
  );
  background-position: 0 14px;
  border: 1px solid var(--card-border);
  border-left: 3px solid #f2b8b8;
  border-radius: 4px 14px 14px 4px;
  padding: 14px 18px 40px 22px;
  display: flex;
  flex-direction: column;
}
.title,
.content {
  border: none;
  outline: none;
  background: transparent;
  font-family: var(--font-hand);
  line-height: var(--line);
  padding: 0;
  width: 100%;
}
.title {
  height: calc(var(--line) * 1);
  font-size: 24px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 0;
}
.content {
  font-size: 19px;
  min-height: calc(var(--line) * 12);
  resize: none;
  overflow: hidden;
  color: var(--ink);
}
::placeholder {
  color: #c4bdab;
}

.fold-enter-active,
.fold-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.fold-enter-from,
.fold-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
