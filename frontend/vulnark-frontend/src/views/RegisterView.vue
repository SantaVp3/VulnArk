<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1>注册账号</h1>
        <p>创建您的VulnArk账号</p>
      </div>
      
      <a-form
        :model="form"
        :rules="rules"
        @submit="handleSubmit"
        layout="vertical"
        class="register-form"
      >
        <a-form-item field="username" label="用户名">
          <a-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
          />
        </a-form-item>
        
        <a-form-item field="email" label="邮箱">
          <a-input
            v-model="form.email"
            placeholder="请输入邮箱"
            size="large"
          />
        </a-form-item>
        
        <a-form-item field="fullName" label="姓名">
          <a-input
            v-model="form.fullName"
            placeholder="请输入姓名"
            size="large"
          />
        </a-form-item>
        
        <a-form-item field="phone" label="电话">
          <a-input
            v-model="form.phone"
            placeholder="请输入电话号码"
            size="large"
          />
        </a-form-item>
        
        <a-form-item field="password" label="密码">
          <a-input-password
            v-model="form.password"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>
        
        <a-form-item field="confirmPassword" label="确认密码">
          <a-input-password
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            size="large"
          />
        </a-form-item>
        
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            long
            :loading="loading"
          >
            注册
          </a-button>
        </a-form-item>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <a-link @click="$router.push('/login')">立即登录</a-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: '',
  email: '',
  fullName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 3, message: '用户名至少3个字符' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, message: '密码至少6个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (value: string, callback: Function) => {
        if (value !== form.password) {
          callback('两次输入的密码不一致')
        } else {
          callback()
        }
      }
    }
  ]
}

const handleSubmit = async (data: any) => {
  if (data.errors) return
  
  loading.value = true
  try {
    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
      fullName: form.fullName,
      phone: form.phone
    })
    Message.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    Message.error(error.response?.data?.message || '注册失败')
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 28px;
  color: #1d2129;
  margin-bottom: 8px;
}

.register-header p {
  color: #86909c;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #86909c;
}

/* 平板响应式 */
@media (max-width: 768px) {
  .register-card {
    padding: 30px;
    margin: 0 20px;
  }

  .register-header h1 {
    font-size: 24px;
  }
}

/* 手机响应式 */
@media (max-width: 480px) {
  .register-container {
    padding: 15px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .register-card {
    padding: 20px;
    margin: 0 10px;
  }

  .register-header h1 {
    font-size: 20px;
  }

  .register-header p {
    font-size: 12px;
  }
}

/* 超小屏幕 */
@media (max-width: 320px) {
  .register-card {
    padding: 15px;
    margin: 0 5px;
  }
}
</style>
