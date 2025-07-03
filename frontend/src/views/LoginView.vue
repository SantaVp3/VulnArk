<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">欢迎使用 VulnArk</h1>
        <p class="login-subtitle">漏洞管理平台</p>
      </div>
      
      <div class="login-form">
        <a-form
          :model="form"
          :rules="rules"
          @submit="handleSubmit"
          layout="vertical"
          size="large"
        >
          <a-form-item field="username" label="用户名">
            <a-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix="IconUser"
            />
          </a-form-item>
          
          <a-form-item field="password" label="密码">
            <a-input-password
              v-model="form.password"
              placeholder="请输入密码"
              :prefix="IconLock"
            />
          </a-form-item>
          
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              long
              :loading="loading"
              class="login-button"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <a-link @click="$router.push('/register')">立即注册</a-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { IconUser, IconLock } from '@arco-design/web-vue/es/icon'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive<LoginRequest>({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' }
  ]
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await authStore.login(form)
    Message.success('登录成功')
    router.push('/dashboard')
  } catch (error: any) {
    Message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--primary-100) 100%);
  padding: var(--spacing-lg);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--surface);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-2xl);
  animation: fade-in 0.5s ease-out;
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.login-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.login-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

.login-form {
  margin-bottom: var(--spacing-lg);
}

.login-button {
  height: 48px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-600), var(--primary-700));
  border: none;
  border-radius: var(--radius-lg);
  transition: var(--transition-normal);
}

.login-button:hover {
  background: linear-gradient(135deg, var(--primary-700), var(--primary-800));
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.login-footer {
  text-align: center;
  color: var(--text-secondary);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border);
}

.login-footer a {
  color: var(--primary-600);
  font-weight: 500;
  margin-left: var(--spacing-xs);
}

@media (max-width: 480px) {
  .login-container {
    padding: var(--spacing-md);
  }
  
  .login-card {
    padding: var(--spacing-xl);
  }
  
  .login-title {
    font-size: 1.5rem;
  }
}
</style> 