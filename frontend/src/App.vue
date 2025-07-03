<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  // 检查本地存储的认证状态
  authStore.checkAuth()
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style> 