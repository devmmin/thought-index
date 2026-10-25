import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { installAuth } from './stores/auth'
import './styles/base.css'

const pinia = createPinia()
installAuth(pinia, router)

createApp(App).use(pinia).use(router).mount('#app')
