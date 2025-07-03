<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被移动。
      </p>
      <div class="error-actions">
        <a-button type="primary" @click="goHome" size="large">
          返回首页
        </a-button>
        <a-button @click="goBack" size="large">
          返回上页
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--background);
  padding: var(--spacing-xl);
}

.not-found-content {
  text-align: center;
  max-width: 500px;
}

.error-code {
  font-size: 8rem;
  font-weight: 700;
  color: var(--primary-500);
  line-height: 1;
  margin-bottom: var(--spacing-lg);
}

.error-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.error-description {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin-bottom: var(--spacing-2xl);
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

@media (max-width: 480px) {
  .error-code {
    font-size: 5rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style> 