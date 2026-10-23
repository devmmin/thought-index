import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { tabsApi } from '@/api/tabs'

/**
 * 탭 목록은 목록 화면·편집 화면·검색 화면이 다 같이 쓰니까 전역 store 에 둔다.
 * 메모 목록은 화면마다 따로라 각 화면이 직접 불러온다.
 */
export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref([])
  const loaded = ref(false)

  const byId = computed(() => new Map(tabs.value.map((t) => [t.id, t])))

  async function fetchTabs() {
    tabs.value = await tabsApi.list()
    loaded.value = true
  }

  async function ensureLoaded() {
    if (!loaded.value) await fetchTabs()
  }

  async function createTab(payload) {
    const created = await tabsApi.create(payload)
    tabs.value.push(created)
    return created
  }

  async function updateTab(id, payload) {
    const updated = await tabsApi.update(id, payload)
    tabs.value = tabs.value.map((t) => (t.id === id ? updated : t))
  }

  async function removeTab(id) {
    await tabsApi.remove(id)
    tabs.value = tabs.value.filter((t) => t.id !== id)
  }

  /** 메모를 쓰거나 지운 뒤 탭 옆 숫자를 맞춘다 */
  function bumpCount(tabId, delta) {
    const tab = byId.value.get(tabId)
    if (tab) tab.noteCount = Math.max(0, tab.noteCount + delta)
  }

  return { tabs, loaded, byId, fetchTabs, ensureLoaded, createTab, updateTab, removeTab, bumpCount }
})
