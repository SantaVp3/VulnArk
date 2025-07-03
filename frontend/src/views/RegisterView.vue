<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1 class="register-title">注册账号</h1>
        <p class="register-subtitle">创建您的 VulnArk 账号</p>
      </div>
      
      <div class="register-form">
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
          
          <a-form-item field="fullName" label="姓名">
            <a-input
              v-model="form.fullName"
              placeholder="请输入真实姓名"
              :prefix="IconIdcard"
            />
          </a-form-item>
          
          <a-form-item field="email" label="邮箱">
            <a-input
              v-model="form.email"
              placeholder="请输入邮箱地址"
              :prefix="IconEmail"
            />
          </a-form-item>
          
          <a-form-item field="phone" label="手机号">
            <a-input
              v-model="form.phone"
              placeholder="请输入手机号"
              :prefix="IconPhone"
            />
          </a-form-item>
          
          <a-form-item field="password" label="密码">
            <a-input-password
              v-model="form.password"
              placeholder="请输入密码"
              :prefix="IconLock"
            />
          </a-form-item>
          
          <a-form-item field="confirmPassword" label="确认密码">
            <a-input-password
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              :prefix="IconLock"
            />
          </a-form-item>
          
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              long
              :loading="loading"
              class="register-button"
            >
              注册
            </a-button>
          </a-form-item>
        </a-form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <a-link @click="$router.push('/login')">立即登录</a-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { 
  IconUser, 
  IconLock, 
  IconIdcard, 
  IconEmail, 
  IconPhone 
} from '@arco-design/web-vue/es/icon'
import { register } from '@/api/auth'
import type { RegisterRequest } from '@/types/auth'

const router = useRouter()

const loading = ref(false)
const form = reactive<RegisterRequest>({
  username: '',
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 3, message: '用户名至少3位' }
  ],
  fullName: [
    { required: true, message: '请输入真实姓名' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址' },
    { type: 'email', message: '请输入正确的邮箱格式' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (value: string, callback: (error?: string) => void) => {
        if (value !== form.password) {
          callback('两次输入的密码不一致')
        } else {
          callback()
        }
      }
    }
  ]
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await register(form)
    Message.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    Message.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--primary-100) 100%);
  padding: var(--spacing-lg);
}

.register-card {
  width: 100%;
  max-width: 450px;
  background: var(--surface);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-2xl);
  animation: fade-in 0.5s ease-out;
}

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.register-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.register-subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
}

.register-form {
  margin-bottom: var(--spacing-lg);
}

.register-button {
  height: 48px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-600), var(--primary-700));
  border: none;
  border-radius: var(--radius-lg);
  transition: var(--transition-normal);
}

.register-button:hover {
  background: linear-gradient(135deg, var(--primary-700), var(--primary-800));
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.register-footer {
  text-align: center;
  color: var(--text-secondary);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border);
}

.register-footer a {
  color: var(--primary-600);
  font-weight: 500;
  margin-left: var(--spacing-xs);
}

@media (max-width: 480px) {
  .register-container {
    padding: var(--spacing-md);
  }
  
  .register-card {
    padding: var(--spacing-xl);
  }
  
  .register-title {
    font-size: 1.5rem;
  }
}
</style> 