<template>
  <div class="debug-container">
    <h1>调试页面</h1>
    
    <div class="debug-section">
      <h2>API连接测试</h2>
      <a-button @click="testHealthAPI" :loading="healthLoading">测试健康检查API</a-button>
      <div v-if="healthResult" class="result">
        <pre>{{ JSON.stringify(healthResult, null, 2) }}</pre>
      </div>
    </div>
    
    <div class="debug-section">
      <h2>登录测试</h2>
      <a-form :model="loginForm" @submit="testLogin" layout="inline">
        <a-form-item label="用户名">
          <a-input v-model="loginForm.username" placeholder="testuser" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model="loginForm.password" placeholder="password123" />
        </a-form-item>
        <a-form-item>
          <a-button html-type="submit" type="primary" :loading="loginLoading">测试登录</a-button>
        </a-form-item>
      </a-form>
      <div v-if="loginResult" class="result">
        <pre>{{ JSON.stringify(loginResult, null, 2) }}</pre>
      </div>
    </div>
    
    <div class="debug-section">
      <h2>认证状态</h2>
      <p>Token: {{ authStore.token ? '已设置' : '未设置' }}</p>
      <p>用户: {{ authStore.user ? authStore.user.username : '未登录' }}</p>
      <p>登录状态: {{ authStore.isLoggedIn ? '已登录' : '未登录' }}</p>
      <a-button @click="clearAuth">清除认证信息</a-button>
    </div>
    


    <div class="debug-section">
      <h2>路由测试</h2>
      <a-button @click="$router.push('/login')">跳转到登录页</a-button>
      <a-button @click="$router.push('/dashboard')">跳转到仪表板</a-button>
      <a-button @click="$router.push('/register')">跳转到注册页</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/index'

const authStore = useAuthStore()

const healthLoading = ref(false)
const healthResult = ref(null)

const loginLoading = ref(false)
const loginResult = ref(null)
const loginForm = reactive({
  username: 'admin',
  password: 'password123'
})

const testHealthAPI = async () => {
  healthLoading.value = true
  try {
    const response = await fetch('http://localhost:8080/api/test/health')
    const data = await response.json()
    healthResult.value = data
  } catch (error) {
    healthResult.value = { error: error.message }
  } finally {
    healthLoading.value = false
  }
}

const testLogin = async () => {
  loginLoading.value = true
  try {
    const response = await api.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    loginResult.value = response
  } catch (error) {
    loginResult.value = { error: error.message }
  } finally {
    loginLoading.value = false
  }
}

const clearAuth = () => {
  authStore.logout()
  loginResult.value = null
}
</script>

<style scoped>
.debug-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.debug-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.result {
  margin-top: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
}

h1 {
  color: #1d2129;
  margin-bottom: 20px;
}

h2 {
  color: #4e5969;
  margin-bottom: 15px;
  font-size: 16px;
}
</style>
