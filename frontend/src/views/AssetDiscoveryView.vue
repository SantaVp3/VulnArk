<template>
  <div class="asset-discovery-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">资产发现</h1>
        <p class="page-description">自动发现和识别网络资产</p>
      </div>
    </div>
    
    <div class="coming-soon">
      <a-empty description="功能开发中...">
        <template #image>
          <icon-computer />
        </template>
      </a-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IconComputer } from '@arco-design/web-vue/es/icon'
</script>

<style scoped>
.asset-discovery-page {
  width: 100%;
  padding: var(--spacing-lg);
  min-height: 100vh;
  background: var(--background);
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-description {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 0;
}

.coming-soon {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style> 