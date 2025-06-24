<template>
  <div class="responsive-test">
    <div class="responsive-container">
      <h1>响应式设计测试页面</h1>
      
      <a-card class="test-card">
        <h2>当前屏幕信息</h2>
        <p>屏幕宽度: {{ screenWidth }}px</p>
        <p>屏幕高度: {{ screenHeight }}px</p>
        <p>设备类型: {{ deviceType }}</p>
      </a-card>
      
      <a-row :gutter="[16, 16]" class="test-grid">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 1</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 2</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 3</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 4</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 5</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
          <a-card class="grid-item">
            <h3>卡片 6</h3>
            <p>xs:24 sm:12 md:8 lg:6 xl:4</p>
          </a-card>
        </a-col>
      </a-row>
      
      <div class="visibility-test">
        <h2>可见性测试</h2>
        <p class="hidden-xs">在超小屏幕上隐藏 (hidden-xs)</p>
        <p class="hidden-sm">在小屏幕上隐藏 (hidden-sm)</p>
        <p class="hidden-md">在中等屏幕上隐藏 (hidden-md)</p>
        <p class="hidden-lg">在大屏幕上隐藏 (hidden-lg)</p>
        <p class="hidden-xl">在超大屏幕上隐藏 (hidden-xl)</p>
      </div>
      
      <div class="button-test">
        <h2>按钮测试</h2>
        <a-button type="primary" class="btn-responsive">响应式按钮</a-button>
        <a-button type="outline" class="btn-responsive">轮廓按钮</a-button>
        <a-button type="text" class="btn-responsive">文本按钮</a-button>
      </div>
      
      <div class="table-test">
        <h2>表格测试</h2>
        <div class="table-responsive">
          <a-table :data="tableData" :pagination="false">
            <a-table-column title="ID" data-index="id" />
            <a-table-column title="名称" data-index="name" />
            <a-table-column title="类型" data-index="type" />
            <a-table-column title="状态" data-index="status" />
            <a-table-column title="创建时间" data-index="createTime" />
            <a-table-column title="操作" data-index="action" />
          </a-table>
        </div>
      </div>
      
      <div class="back-button">
        <a-button @click="goBack">返回登录页面</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const screenWidth = ref(0)
const screenHeight = ref(0)
const deviceType = ref('')

const tableData = ref([
  { id: 1, name: '测试项目1', type: 'Web应用', status: '活跃', createTime: '2023-12-01', action: '查看' },
  { id: 2, name: '测试项目2', type: '移动应用', status: '暂停', createTime: '2023-12-02', action: '编辑' },
  { id: 3, name: '测试项目3', type: 'API服务', status: '活跃', createTime: '2023-12-03', action: '删除' },
])

const updateScreenInfo = () => {
  screenWidth.value = window.innerWidth
  screenHeight.value = window.innerHeight
  
  if (screenWidth.value < 480) {
    deviceType.value = '超小屏幕 (xs)'
  } else if (screenWidth.value < 576) {
    deviceType.value = '小屏幕 (sm)'
  } else if (screenWidth.value < 768) {
    deviceType.value = '中等屏幕 (md)'
  } else if (screenWidth.value < 992) {
    deviceType.value = '大屏幕 (lg)'
  } else {
    deviceType.value = '超大屏幕 (xl)'
  }
}

const goBack = () => {
  router.push('/login')
}

onMounted(() => {
  updateScreenInfo()
  window.addEventListener('resize', updateScreenInfo)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenInfo)
})
</script>

<style scoped>
.responsive-test {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #f5f5f5;
  padding: 20px;
  box-sizing: border-box;
  overflow: auto;
}

.responsive-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.test-card {
  margin-bottom: 24px;
  text-align: center;
}

.test-grid {
  margin-bottom: 24px;
}

.grid-item {
  text-align: center;
  min-height: 120px;
}

.visibility-test {
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.button-test {
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.button-test .btn-responsive {
  margin-right: 8px;
  margin-bottom: 8px;
}

.table-test {
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.back-button {
  text-align: center;
  padding: 24px;
}

h1, h2 {
  color: #1d2129;
  margin-bottom: 16px;
}

h1 {
  text-align: center;
  font-size: 28px;
}

h2 {
  font-size: 20px;
}

/* 可见性测试样式 */
.hidden-xs {
  display: block;
}

.hidden-sm {
  display: block;
}

.hidden-md {
  display: block;
}

.hidden-lg {
  display: block;
}

.hidden-xl {
  display: block;
}

/* 响应式隐藏 */
@media (max-width: 320px) {
  .hidden-xs {
    display: none;
  }
}

@media (max-width: 576px) {
  .hidden-sm {
    display: none;
  }
}

@media (max-width: 768px) {
  .hidden-md {
    display: none;
  }
}

@media (max-width: 992px) {
  .hidden-lg {
    display: none;
  }
}

@media (min-width: 1200px) {
  .hidden-xl {
    display: none;
  }
}

/* 表格响应式 */
.table-responsive {
  overflow-x: auto;
}

@media (max-width: 768px) {
  h1 {
    font-size: 24px;
  }
  
  h2 {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .responsive-test {
    padding: 10px 0;
  }
  
  h1 {
    font-size: 20px;
  }
  
  h2 {
    font-size: 16px;
  }
}
</style>
