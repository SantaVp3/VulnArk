import './assets/main.css'
import './styles/responsive.css'
import '@arco-design/web-vue/dist/arco.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ArcoVue from '@arco-design/web-vue'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ArcoVue)

// 初始化认证状态
const authStore = useAuthStore()
authStore.initializeAuth()

app.mount('#app')
