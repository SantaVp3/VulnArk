<template>
  <div class="scan-view">
    <!-- 使用新的扫描管理组件 -->
    <ScanManagement />

  </div>
</template>

<script setup lang="ts">
import ScanManagement from './scan/ScanManagement.vue'
</script>

<style scoped>
.scan-view {
  height: 100%;
}
</style>
