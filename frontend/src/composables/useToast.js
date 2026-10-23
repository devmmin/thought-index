import { ref } from 'vue'

const message = ref('')
let timer

/** 화면 아래에 잠깐 뜨는 안내 문구. 어디서든 같은 상태를 공유한다 */
export function useToast() {
  function show(text) {
    message.value = text
    clearTimeout(timer)
    timer = setTimeout(() => (message.value = ''), 2200)
  }
  return { message, show }
}
