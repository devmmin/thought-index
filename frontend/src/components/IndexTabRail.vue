<script setup>
import { ref, watch } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { Plus } from 'lucide-vue-next'
import { MAX_TABS, paletteOf } from '@/constants/tabColors'

const props = defineProps({
  tabs: { type: Array, required: true },
  activeId: { type: Number, default: null },
})
const emit = defineEmits(['select', 'edit', 'add', 'reorder'])

// 드래그 라이브러리는 배열을 직접 바꾸므로 props 를 복사해서 넘긴다
const list = ref([...props.tabs])
watch(
  () => props.tabs,
  (tabs) => (list.value = [...tabs]),
)

function onEnd() {
  const ids = list.value.map((t) => t.id)
  const changed = ids.some((id, i) => id !== props.tabs[i]?.id)
  if (changed) emit('reorder', ids)
}
</script>

<template>
  <nav class="rail" aria-label="인덱스">
    <!-- 마우스는 바로 끌기, 터치는 0.25초 꾹 눌러야 끌기 (그냥 탭하면 선택).
         forceFallback: 브라우저 기본 드래그(HTML5 DnD) 대신 일반 포인터 이벤트로 끌어서 모바일에서도 똑같이 동작 -->
    <VueDraggable
      v-model="list"
      class="list"
      :animation="180"
      :delay="250"
      :delay-on-touch-only="true"
      :force-fallback="true"
      fallback-class="dragging"
      ghost-class="ghost"
      chosen-class="chosen"
      @end="onEnd"
    >
      <button
        v-for="tab in list"
        :key="tab.id"
        class="tab"
        :class="{ active: tab.id === activeId }"
        :style="{
          '--tab-bg': tab.id === activeId ? paletteOf(tab.color).tint : paletteOf(tab.color).tab,
          '--tab-ink': paletteOf(tab.color).ink,
        }"
        :aria-current="tab.id === activeId ? 'page' : undefined"
        :title="tab.id === activeId ? '한 번 더 누르면 고칠 수 있어요 · 꾹 눌러 끌면 순서 바꾸기' : tab.name"
        @click="tab.id === activeId ? emit('edit', tab) : emit('select', tab)"
      >
        <span class="label" :class="{ long: tab.name.length > 6 }">{{ tab.name }}</span>
      </button>
    </VueDraggable>

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
.list {
  display: flex;
  flex-direction: column;
  gap: 6px;
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
  touch-action: pan-y;
}
.tab:active {
  transform: scaleY(0.97);
}
.tab.active {
  width: 36px;
}

/* 끌고 있는 탭은 살짝 들어 올린 느낌, 빈자리는 흐리게 */
.chosen {
  transform: translateX(4px) scale(1.04);
}
.ghost {
  opacity: 0.35;
}
.dragging {
  opacity: 0.9;
}

.label {
  writing-mode: vertical-rl;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 1px;
  white-space: nowrap;
  max-height: 100px;
  overflow: hidden;
}
.label.long {
  mask-image: linear-gradient(to bottom, #000 72%, transparent);
}
.tab.active .label {
  font-weight: 700;
}

.add {
  min-height: 38px;
  background: #ede8dc;
  color: var(--ink-muted);
}
</style>
