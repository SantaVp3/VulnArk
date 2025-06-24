<template>
  <div class="login-container login-container-enhanced">
    <div class="login-background-effects">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
    <div class="login-card login-card-enhanced">
      <div class="login-header login-header-enhanced">
        <div class="logo-container">
          <icon-safe class="logo-icon" />
          <h1 class="gradient-text">VulnArk</h1>
        </div>
        <p class="subtitle">企业级漏洞管理平台</p>
        <div class="security-badge">
          <icon-safe class="security-icon" />
          <span>安全可信</span>
        </div>
      </div>
      
      <a-form
        :model="form"
        :rules="rules"
        @submit="handleSubmit"
        layout="vertical"
        class="login-form"
      >
        <a-form-item field="username" label="用户名" class="form-item-enhanced">
          <a-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            class="input-enhanced"
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>
        
        <a-form-item field="password" label="密码" class="form-item-enhanced">
          <a-input-password
            v-model="form.password"
            placeholder="请输入密码"
            size="large"
            class="input-enhanced"
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
        
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            long
            :loading="loading"
            class="btn-enhanced login-btn-enhanced"
          >
            <icon-right v-if="!loading" />
            登录
          </a-button>
        </a-form-item>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <a-link @click="$router.push('/register')">立即注册</a-link>
          <br>
          <a-link @click="$router.push('/responsive-test')" style="font-size: 12px; color: #86909c;">响应式测试页面</a-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { IconUser, IconLock, IconSafe, IconRight } from '@arco-design/web-vue/es/icon'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码' }
  ]
}

const handleSubmit = async (data: any) => {
  if (data.errors) return

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    Message.success('登录成功')

    // 等待一小段时间确保状态更新
    await new Promise(resolve => setTimeout(resolve, 100))

    try {
      await router.push('/dashboard')
    } catch (navError) {
      console.error('Router navigation failed:', navError)
      window.location.href = '/dashboard'
    }
  } catch (error: any) {
    console.error('Login error:', error)
    Message.error(error.message || error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 重置样式 */
* {
  box-sizing: border-box;
}
.login-container-enhanced {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 50%, #1e40af 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: auto;
  z-index: 1000;
  position: relative;
}

.login-background-effects {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.login-card-enhanced {
  width: 100%;
  max-width: 420px;
  min-width: 320px;
  padding: 48px;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
  box-sizing: border-box;
  margin: auto;
  position: relative;
  backdrop-filter: blur(10px);
  overflow: hidden;
}

.login-card-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.login-header-enhanced {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.logo-icon {
  font-size: 36px;
  color: var(--primary-color);
  animation: pulse 2s infinite;
}

.login-header-enhanced h1 {
  font-size: 32px;
  margin: 0;
  font-weight: 700;
  letter-spacing: 1px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 16px;
  margin: var(--spacing-md) 0;
  font-weight: 400;
}

.security-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  border-radius: var(--radius-md);
  color: var(--status-success);
  font-size: 12px;
  font-weight: 500;
  margin-top: var(--spacing-md);
}

.security-icon {
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #86909c;
}

/* 平板响应式 */
@media (max-width: 768px) {
  .login-card {
    padding: 30px;
    margin: 0 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }
}

/* 手机响应式 */
@media (max-width: 480px) {
  .login-container {
    padding: 15px;
  }

  .login-card {
    padding: 20px;
    margin: 0 10px;
  }

  .login-header h1 {
    font-size: 20px;
  }

  .login-header p {
    font-size: 12px;
  }
}

/* 超小屏幕 */
@media (max-width: 320px) {
  .login-card {
    padding: 15px;
    margin: 0 5px;
  }
}

/* 增强样式 */
.form-item-enhanced {
  margin-bottom: var(--spacing-lg);
}

.input-enhanced {
  border-radius: var(--radius-md) !important;
  border: 2px solid var(--border-light) !important;
  transition: all var(--transition-fast) !important;
}

.input-enhanced:hover {
  border-color: var(--primary-light) !important;
}

.input-enhanced:focus {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1) !important;
}

.login-btn-enhanced {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: var(--radius-md) !important;
  background: var(--primary-gradient) !important;
  border: none !important;
  box-shadow: var(--shadow-md) !important;
  transition: all var(--transition-fast) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: var(--spacing-sm) !important;
}

.login-btn-enhanced:hover {
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary-color) 100%) !important;
}

.login-btn-enhanced:active {
  transform: translateY(0) !important;
}

/* 表单标签样式 */
:deep(.arco-form-item-label) {
  font-weight: 600 !important;
  color: var(--text-primary) !important;
  margin-bottom: var(--spacing-sm) !important;
}

/* 输入框前缀图标样式 */
:deep(.arco-input-prefix) {
  color: var(--primary-color) !important;
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  33% {
    transform: translateY(-20px) rotate(5deg);
  }
  66% {
    transform: translateY(10px) rotate(-5deg);
  }
}
</style>
