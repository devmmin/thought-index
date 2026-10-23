<script setup>
import { computed } from 'vue'
import { Pin } from 'lucide-vue-next'
import { formatDate } from '@/composables/formatDate'
import { paletteOf } from '@/constants/tabColors'

const props = defineProps({
  note: { type: Object, required: true },
  accent: { type: String, required: true },
  /** 검색 결과처럼 여러 탭이 섞일 때 탭 이름을 같이 보여준다 */
  tab: { type: Object, default: null },
})

const preview = computed(() => props.note.content.trim())
const chip = computed(() => props.tab && paletteOf(props.tab.color))
</script>

<template>
  <RouterLink :to="`/notes/${note.id}`" class="card" :class="{ pinned: note.pinned }" :style="{ '--accent': accent }">
    <div class="top">
      <h3 class="title">{{ note.title }}</h3>
      <Pin v-if="note.pinned" class="pin" :size="14" :stroke-width="2.2" aria-label="고정됨" />
    </div>
    <p v-if="preview" class="preview">{{ preview }}</p>
    <div class="meta">
      <span v-if="tab" class="chip" :style="{ background: chip.tab, color: chip.ink }">{{ tab.name }}</span>
      <time :datetime="note.updatedAt">{{ formatDate(note.updatedAt) }}</time>
    </div>
  </RouterLink>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-card);
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s;
}
.card:active {
  transform: scale(0.985);
}
.card.pinned {
  border-left: 4px solid var(--accent);
  border-radius: 4px var(--radius-card) var(--radius-card) 4px;
}

.top {
  display: flex;
  align-items: center;
  gap: 6px;
}
.title {
  flex: 1;
  margin: 0;
  font-size: 15px;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pin {
  color: var(--accent);
  flex-shrink: 0;
}

.preview {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--ink-soft);
  white-space: pre-line;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-muted);
}
.chip {
  padding: 1px 8px;
  border-radius: 999px;
}
</style>
