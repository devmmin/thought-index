<script setup>
import { Plus } from 'lucide-vue-next'
import { MAX_TABS, paletteOf } from '@/constants/tabColors'

defineProps({
  tabs: { type: Array, required: true },
  activeId: { type: Number, default: null },
})
const emit = defineEmits(['select', 'edit', 'add'])
</script>

<template>
  <nav class="rail" aria-label="인덱스">
    <TransitionGroup name="tab">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab"
        :class="{ active: tab.id === activeId }"
        :style="{
          '--tab-bg': tab.id === activeId ? paletteOf(tab.color).tint : paletteOf(tab.color).tab,
          '--tab-ink': paletteOf(tab.color).ink,
        }"
        :aria-current="tab.id === activeId ? 'page' : undefined"
        :title="tab.id === activeId ? '한 번 더 누르면 인덱스를 고칠 수 있어요' : tab.name"
        @click="tab.id === activeId ? emit('edit', tab) : emit('select', tab)"
      >
        <span class="label">{{ tab.name }}</span>
      </button>
    </TransitionGroup>

    <button v-if="tabs.length < MAX_TABS" class="tab add" aria-label="인덱스 붙이기" @click="emit('add')">
      <Plus :size="14" :stroke-width="2.4" />
    </button>
  </nav>
</template>

<style scoped>
.rail {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 10px;
  position: sticky;
  top: 12px;
  align-self: flex-start;
}

.tab {
  width: 28px;
  min-height: 62px;
  padding: 10px 0;
  border-radius: 0 10px 10px 0;
  background: var(--tab-bg);
  color: var(--tab-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: width 0.25s var(--ease-soft), background-color 0.2s;
}
.tab:active {
  transform: scaleY(0.97);
}
.tab.active {
  width: 36px;
}

.label {
  writing-mode: vertical-rl;
  font-size: 13px;
  letter-spacing: 1px;
  max-height: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add {
  min-height: 38px;
  background: #ede8dc;
  color: var(--ink-muted);
}

.tab-enter-active {
  transition: transform 0.3s var(--ease-soft), opacity 0.2s;
}
.tab-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}
.tab-leave-active {
  transition: transform 0.2s, opacity 0.2s;
}
.tab-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}
</style>
