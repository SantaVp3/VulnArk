<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app">
    <RouterView />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  overflow-x: hidden;
}

#app {
  height: 100vh;
  width: 100vw;
  min-height: 100vh;
  min-width: 320px; /* 最小宽度支持 */
}

/* 响应式断点 */
@media (max-width: 768px) {
  #app {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  #app {
    font-size: 12px;
  }
}
</style>
