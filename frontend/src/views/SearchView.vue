<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, Search } from 'lucide-vue-next'
import NoteCard from '@/components/NoteCard.vue'
import { notesApi } from '@/api/notes'
import { paletteOf } from '@/constants/tabColors'
import { useTabsStore } from '@/stores/tabs'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const tabsStore = useTabsStore()
const toast = useToast()

const q = ref('')
const results = ref([])
const searched = ref(false)
const inputEl = ref(null)

onMounted(() => {
  tabsStore.ensureLoaded().catch((e) => toast.show(e.message))
  inputEl.value?.focus()
})

// 글자를 칠 때마다가 아니라, 멈추고 0.3초 뒤에 검색 (요청 수 줄이기)
let timer
let seq = 0
watch(q, (value) => {
  clearTimeout(timer)
  if (!value.trim()) {
    results.value = []
    searched.value = false
    return
  }
  timer = setTimeout(async () => {
    const mine = ++seq
    try {
      const res = await notesApi.search(value.trim())
      // 늦게 도착한 옛날 응답이 최신 결과를 덮어쓰지 않게
      if (mine === seq) {
        results.value = res.content
        searched.value = true
      }
    } catch (e) {
      toast.show(e.message)
    }
  }, 300)
})
</script>

<template>
  <div class="search">
    <header class="bar">
      <button class="icon" aria-label="뒤로" @click="router.back()"><ChevronLeft :size="24" /></button>
      <label class="box">
        <Search :size="18" />
        <input ref="inputEl" v-model="q" type="search" placeholder="메모 찾기" aria-label="검색어" />
      </label>
    </header>

    <div class="list">
      <NoteCard
        v-for="note in results"
        :key="note.id"
        :note="note"
        :tab="tabsStore.byId.get(note.tabId)"
        :accent="paletteOf(tabsStore.byId.get(note.tabId)?.color).accent"
      />
    </div>
    <p v-if="searched && !results.length" class="empty">
      <span class="keyword">'{{ q.trim() }}'</span>가 들어간 메모가 없어요.
    </p>
  </div>
</template>

<style scoped>
.search {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: calc(8px + env(safe-area-inset-top)) 14px 24px;
}
.bar {
  display: flex;
  align-items: center;
  gap: 6px;
}
.icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-soft);
}
.box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 14px;
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: 14px;
  color: var(--ink-muted);
}
.box input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: var(--ink);
}
.box input::-webkit-search-cancel-button {
  display: none;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.keyword {
  display: inline-block;
  max-width: 12em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  color: var(--ink-soft);
}
.empty {
  text-align: center;
  color: var(--ink-muted);
  font-size: 14px;
  padding-top: 40px;
}
</style>
