<script setup>
import { computed, ref, watch } from 'vue'
import { COLOR_KEYS, TAB_COLORS, paletteOf } from '@/constants/tabColors'

const props = defineProps({
  open: { type: Boolean, default: false },
  /** null 이면 새 인덱스 만들기, 있으면 고치기 */
  tab: { type: Object, default: null },
})
const emit = defineEmits(['close', 'submit', 'remove'])

const name = ref('')
const color = ref('PEACH')
const confirmingRemove = ref(false)
const error = ref('')

watch(
  () => props.open,
  (open) => {
    if (!open) return
    name.value = props.tab?.name ?? ''
    color.value = props.tab?.color ?? COLOR_KEYS[0]
    confirmingRemove.value = false
    error.value = ''
  },
)

const NAME_MAX = 20 // 서버 IndexTabRequest 의 @Size(max = 20) 과 맞춘다
const palette = computed(() => paletteOf(color.value))

function submit() {
  if (!name.value.trim()) {
    error.value = '인덱스 이름을 적어 주세요.'
    return
  }
  emit('submit', { name: name.value.trim(), color: color.value })
}
</script>

<template>
  <Teleport to="body">
    <Transition name="sheet">
      <div v-if="open" class="backdrop" @click.self="emit('close')">
        <form class="sheet" @submit.prevent="submit">
          <div class="grip" />
          <h2>{{ tab ? '인덱스 고치기' : '새 인덱스 붙이기' }}</h2>

          <div class="preview">
            <div class="paper" />
            <div
              class="sticker"
              :class="{ long: name.trim().length > 6 }"
              :style="{ background: palette.tab, color: palette.ink }"
            >
              {{ name.trim() || '이름' }}
            </div>
          </div>

          <label class="field">
            <span class="field-head">
              이름
              <span class="counter" :class="{ near: name.length >= NAME_MAX - 3 }">{{ name.length }}/{{ NAME_MAX }}</span>
            </span>
            <input v-model="name" :maxlength="NAME_MAX" placeholder="아이디어" @input="error = ''" />
          </label>
          <p v-if="name.trim().length > 6" class="hint">탭에는 앞 6~7글자가 보여요. 짧을수록 한눈에 들어와요.</p>
          <p v-if="error" class="error">{{ error }}</p>

          <fieldset class="field">
            <legend>색</legend>
            <div class="swatches">
              <button
                v-for="key in COLOR_KEYS"
                :key="key"
                type="button"
                class="swatch"
                :class="{ selected: key === color }"
                :style="{ background: TAB_COLORS[key].tab, '--ring': TAB_COLORS[key].accent }"
                :aria-label="TAB_COLORS[key].label"
                :aria-pressed="key === color"
                @click="color = key"
              />
            </div>
          </fieldset>

          <button type="submit" class="primary" :style="{ background: palette.accent }">
            {{ tab ? '저장' : '붙이기' }}
          </button>

          <template v-if="tab">
            <button v-if="!confirmingRemove" type="button" class="danger-link" @click="confirmingRemove = true">
              인덱스 떼어내기
            </button>
            <div v-else class="confirm">
              <p>
                '{{ tab.name }}'와 안에 있는 메모 {{ tab.noteCount }}개가 모두 지워져요.
              </p>
              <div class="confirm-actions">
                <button type="button" class="ghost" @click="confirmingRemove = false">그만두기</button>
                <button type="button" class="danger" @click="emit('remove', tab)">떼어내기</button>
              </div>
            </div>
          </template>
        </form>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(62, 58, 48, 0.32);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 40;
}
.sheet {
  width: 100%;
  max-width: 440px;
  background: var(--paper);
  border-radius: 22px 22px 0 0;
  padding: 10px 20px calc(24px + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.grip {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: var(--paper-line);
  align-self: center;
}
h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: var(--tracking-tight);
}

.preview {
  display: flex;
  justify-content: center;
  padding: 4px 0;
}
.paper {
  width: 120px;
  height: 80px;
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: 8px 0 8px 8px;
}
.sticker {
  writing-mode: vertical-rl;
  font-size: 13px;
  padding: 10px 6px;
  border-radius: 0 10px 10px 0;
  align-self: flex-start;
  margin-top: 8px;
  max-height: 100px;
  white-space: nowrap;
  font-weight: 600;
  overflow: hidden;
  transition: background-color 0.2s;
}
.sticker.long {
  mask-image: linear-gradient(to bottom, #000 72%, transparent);
}
.paper {
  height: 100px;
}
.field-head {
  display: flex;
  justify-content: space-between;
}
.counter {
  font-size: 12px;
  color: var(--ink-muted);
  font-variant-numeric: tabular-nums;
}
.counter.near {
  color: #c27a2c;
}
.hint {
  margin: -6px 0 0;
  font-size: 12px;
  color: var(--ink-muted);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: none;
  margin: 0;
  padding: 0;
  font-size: 13px;
  color: var(--ink-soft);
}
.field legend {
  padding: 0;
  margin-bottom: 6px;
}
input {
  height: 44px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid var(--card-border);
  background: var(--card);
  font-size: 16px;
  outline: none;
}
input:focus {
  border-color: var(--ink-muted);
}
.error {
  margin: -6px 0 0;
  font-size: 13px;
  color: var(--danger);
}

.swatches {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.swatch {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: transform 0.2s var(--ease-soft), box-shadow 0.2s;
}
.swatch.selected {
  transform: scale(1.1);
  box-shadow: 0 0 0 3px var(--paper), 0 0 0 5px var(--ring);
}

.primary {
  height: 48px;
  border-radius: 14px;
  color: #fff;
  font-size: 16px;
  margin-top: 4px;
}
.danger-link {
  color: var(--danger);
  font-size: 14px;
  padding: 6px;
}
.confirm {
  background: #fbeaea;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  color: #7a3030;
}
.confirm p {
  margin: 0 0 10px;
  line-height: 1.5;
}
.confirm-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.ghost,
.danger {
  padding: 8px 14px;
  border-radius: 10px;
}
.danger {
  background: var(--danger);
  color: #fff;
}

.sheet-enter-active,
.sheet-leave-active {
  transition: opacity 0.2s;
}
.sheet-enter-active .sheet,
.sheet-leave-active .sheet {
  transition: transform 0.28s var(--ease-soft);
}
.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}
.sheet-enter-from .sheet,
.sheet-leave-to .sheet {
  transform: translateY(100%);
}
</style>
