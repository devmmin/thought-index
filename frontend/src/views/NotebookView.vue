<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { CircleUserRound, PencilLine, Search } from 'lucide-vue-next'
import IndexTabRail from '@/components/IndexTabRail.vue'
import NoteCard from '@/components/NoteCard.vue'
import TabFormSheet from '@/components/TabFormSheet.vue'
import { notesApi } from '@/api/notes'
import { paletteOf } from '@/constants/tabColors'
import { useTabsStore } from '@/stores/tabs'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  tabId: { type: Number, default: null },
})

const router = useRouter()
const tabsStore = useTabsStore()
const auth = useAuthStore()
const toast = useToast()

const accountOpen = ref(false)
async function toggleAccount() {
  accountOpen.value = !accountOpen.value
  if (accountOpen.value && !auth.member) auth.fetchMe().catch(() => {})
}
function logout() {
  auth.logout()
  router.replace({ name: 'login' })
}

const notes = ref([])
const page = ref(0)
const hasNext = ref(false)
const loading = ref(false)

const sheetOpen = ref(false)
const editingTab = ref(null)

const activeTab = computed(() => tabsStore.byId.get(props.tabId) ?? null)
const palette = computed(() => paletteOf(activeTab.value?.color))

async function loadNotes(reset = true) {
  if (!props.tabId) return
  loading.value = true
  try {
    const nextPage = reset ? 0 : page.value + 1
    const res = await notesApi.listByTab(props.tabId, nextPage)
    notes.value = reset ? res.content : [...notes.value, ...res.content]
    page.value = res.page
    hasNext.value = res.hasNext
  } catch (e) {
    toast.show(e.message)
  } finally {
    loading.value = false
  }
}

// 주소의 tabId 가 바뀔 때마다 (탭을 누를 때마다) 메모를 다시 불러온다
watch(
  () => props.tabId,
  async () => {
    try {
      await tabsStore.ensureLoaded()
    } catch (e) {
      toast.show(e.message)
      return
    }
    // "/" 로 들어왔거나 없는 탭이면 첫 번째 탭으로 보낸다
    if (!activeTab.value && tabsStore.tabs.length) {
      router.replace(`/tabs/${tabsStore.tabs[0].id}`)
      return
    }
    notes.value = []
    await loadNotes()
  },
  { immediate: true },
)

function openCreate() {
  editingTab.value = null
  sheetOpen.value = true
}

function openEdit(tab) {
  editingTab.value = tab
  sheetOpen.value = true
}

async function onSubmitTab(payload) {
  try {
    if (editingTab.value) {
      await tabsStore.updateTab(editingTab.value.id, payload)
      toast.show('인덱스를 고쳤어요')
    } else {
      const created = await tabsStore.createTab(payload)
      router.push(`/tabs/${created.id}`)
      toast.show(`'${created.name}' 인덱스를 붙였어요`)
    }
    sheetOpen.value = false
  } catch (e) {
    toast.show(e.message)
  }
}

async function onReorder(ids) {
  try {
    await tabsStore.reorderTabs(ids)
  } catch (e) {
    toast.show(e.message)
  }
}

async function onRemoveTab(tab) {
  try {
    await tabsStore.removeTab(tab.id)
    sheetOpen.value = false
    toast.show('인덱스를 떼어냈어요')
    router.replace(tabsStore.tabs.length ? `/tabs/${tabsStore.tabs[0].id}` : '/')
  } catch (e) {
    toast.show(e.message)
  }
}
</script>

<template>
  <div class="notebook">
    <header class="header">
      <h1>생각서랍</h1>
      <div class="header-actions">
        <RouterLink to="/search" class="icon-btn" aria-label="검색">
          <Search :size="20" />
        </RouterLink>
        <button class="icon-btn" aria-label="내 계정" :aria-expanded="accountOpen" @click="toggleAccount">
          <CircleUserRound :size="20" />
        </button>
      </div>
    </header>

    <Transition name="fold">
      <div v-if="accountOpen" class="account">
        <span class="account-email">{{ auth.member?.email ?? '…' }}</span>
        <button class="logout" @click="logout">로그아웃</button>
      </div>
    </Transition>

    <!-- 탭이 하나도 없을 때 -->
    <section v-if="tabsStore.loaded && !tabsStore.tabs.length" class="empty-start">
      <p class="hand">아직 붙인 인덱스가 없어요</p>
      <p class="sub">생각을 나눌 첫 칸을 만들어 보세요.</p>
      <button class="start-btn" @click="openCreate">첫 인덱스 붙이기</button>
    </section>

    <main v-else-if="activeTab" class="body">
      <section
        class="panel"
        :style="{ '--tint': palette.tint, '--ink-tab': palette.ink, '--accent': palette.accent }"
      >
        <div class="panel-head">
          <h2>{{ activeTab.name }}</h2>
          <span class="count">{{ activeTab.noteCount }}개</span>
        </div>

        <TransitionGroup name="list" tag="div" class="list">
          <NoteCard v-for="note in notes" :key="note.id" :note="note" :accent="palette.accent" />
        </TransitionGroup>

        <div v-if="!loading && !notes.length" class="empty">
          <p class="hand">아직 비어 있어요</p>
          <p class="sub">떠오른 생각을 적어 꽂아 두세요.</p>
        </div>

        <button v-if="hasNext" class="more" :disabled="loading" @click="loadNotes(false)">더 보기</button>

        <RouterLink :to="`/notes/new?tabId=${activeTab.id}`" class="fab" aria-label="새 메모 쓰기">
          <PencilLine :size="22" />
        </RouterLink>
      </section>

      <IndexTabRail
        :tabs="tabsStore.tabs"
        :active-id="activeTab.id"
        @select="(t) => router.push(`/tabs/${t.id}`)"
        @edit="openEdit"
        @add="openCreate"
        @reorder="onReorder"
      />
    </main>

    <TabFormSheet
      :open="sheetOpen"
      :tab="editingTab"
      @close="sheetOpen = false"
      @submit="onSubmitTab"
      @remove="onRemoveTab"
    />
  </div>
</template>

<style scoped>
.notebook {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: calc(12px + env(safe-area-inset-top)) 0 16px 14px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 18px 14px 6px;
}
h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.04em;
}
.header-actions {
  display: flex;
  gap: 2px;
}
.account {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: -4px 14px 12px 6px;
  padding: 10px 14px;
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: 12px;
  font-size: 13px;
}
.account-email {
  flex: 1;
  min-width: 0;
  color: var(--ink-soft);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.logout {
  color: var(--danger);
  font-size: 13px;
  font-weight: 600;
}
.fold-enter-active,
.fold-leave-active {
  transition: opacity 0.18s, transform 0.18s;
}
.fold-enter-from,
.fold-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
.icon-btn {
  color: var(--ink-muted);
  display: flex;
  padding: 6px;
}

.body {
  flex: 1;
  display: flex;
  align-items: stretch;
}

.panel {
  flex: 1;
  min-width: 0;
  background: var(--tint);
  border-radius: 16px 0 16px 16px;
  padding: 16px 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: background-color 0.25s;
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 0 4px 2px;
}
h2 {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: var(--tracking-tight);
  line-height: 1.35;
  color: var(--ink-tab);
  /* 20자 탭 이름도 두 줄 안에서 끝나게 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.count {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--ink-muted);
  line-height: 27px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty,
.empty-start {
  text-align: center;
  padding: 48px 12px;
}
.empty-start {
  margin-right: 14px;
}
.hand {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: var(--tracking-tight);
  color: var(--ink-soft);
}
.sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--ink-muted);
}
.start-btn {
  margin-top: 20px;
  height: 46px;
  padding: 0 22px;
  border-radius: 14px;
  background: #e8954a;
  color: #fff;
}

.more {
  align-self: center;
  font-size: 13px;
  color: var(--ink-soft);
  padding: 8px 16px;
}

/* 스크롤해도 패널 오른쪽 아래에 붙어 있는 연필 버튼 */
.fab {
  position: sticky;
  bottom: calc(16px + env(safe-area-inset-bottom));
  margin-top: auto;
  align-self: flex-end;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s var(--ease-soft);
}
.fab:active {
  transform: scale(0.92);
}

.list-enter-active {
  transition: opacity 0.25s, transform 0.25s var(--ease-soft);
}
.list-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.list-leave-active {
  display: none;
}
</style>
