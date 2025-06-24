<template>
  <div class="detection-test-view">
    <div class="page-header">
      <h1>资产检测测试</h1>
      <p>测试资产在线状态检测和指纹识别功能</p>
    </div>

    <!-- 快速测试区域 -->
    <a-card title="快速测试" class="test-card">
      <a-form :model="testForm" layout="inline">
        <a-form-item label="目标地址">
          <a-input v-model="testForm.target" placeholder="输入IP地址或域名" style="width: 200px" />
        </a-form-item>
        <a-form-item label="端口">
          <a-input-number v-model="testForm.port" :min="1" :max="65535" placeholder="端口号" style="width: 100px" />
        </a-form-item>
        <a-form-item label="检测类型">
          <a-select v-model="testForm.type" style="width: 150px">
            <a-option value="PING">PING检测</a-option>
            <a-option value="TCP_PORT">TCP端口</a-option>
            <a-option value="HTTP_SERVICE">HTTP服务</a-option>
            <a-option value="HTTPS_SERVICE">HTTPS服务</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="runQuickTest" :loading="testing">
            <template #icon><icon-play-arrow /></template>
            开始测试
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 测试结果 -->
    <a-card v-if="testResult" title="测试结果" class="result-card">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="目标地址">{{ testResult.target }}</a-descriptions-item>
        <a-descriptions-item label="端口">{{ testResult.port || '-' }}</a-descriptions-item>
        <a-descriptions-item label="检测类型">{{ getDetectionTypeLabel(testResult.type) }}</a-descriptions-item>
        <a-descriptions-item label="检测状态">
          <a-tag :color="getDetectionStatusColor(testResult.status)">
            {{ getDetectionStatusLabel(testResult.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="检测结果">
          <a-tag v-if="testResult.result" :color="getDetectionResultColor(testResult.result)">
            {{ getDetectionResultLabel(testResult.result) }}
          </a-tag>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="响应时间">{{ formatResponseTime(testResult.responseTime) }}</a-descriptions-item>
        <a-descriptions-item label="HTTP状态码">{{ testResult.httpStatusCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="错误信息">{{ testResult.errorMessage || '-' }}</a-descriptions-item>
        <a-descriptions-item label="详细信息" :span="2">{{ testResult.details || '-' }}</a-descriptions-item>
        <a-descriptions-item v-if="testResult.banner" label="服务横幅" :span="2">
          <pre>{{ testResult.banner }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <!-- 批量测试区域 -->
    <a-card title="批量测试" class="batch-test-card">
      <div class="batch-controls">
        <a-space>
          <a-button @click="addTestTarget">
            <template #icon><icon-plus /></template>
            添加目标
          </a-button>
          <a-button type="primary" @click="runBatchTest" :loading="batchTesting" :disabled="batchTargets.length === 0">
            <template #icon><icon-play-arrow /></template>
            批量测试
          </a-button>
          <a-button @click="clearBatchTargets" :disabled="batchTargets.length === 0">
            <template #icon><icon-delete /></template>
            清空
          </a-button>
        </a-space>
      </div>

      <!-- 批量目标列表 -->
      <a-table 
        :columns="batchColumns" 
        :data="batchTargets" 
        :pagination="false"
        class="batch-table"
      >
        <template #type="{ record, rowIndex }">
          <a-select v-model="record.type" style="width: 120px">
            <a-option value="PING">PING检测</a-option>
            <a-option value="TCP_PORT">TCP端口</a-option>
            <a-option value="HTTP_SERVICE">HTTP服务</a-option>
            <a-option value="HTTPS_SERVICE">HTTPS服务</a-option>
          </a-select>
        </template>
        <template #target="{ record, rowIndex }">
          <a-input v-model="record.target" placeholder="IP地址或域名" />
        </template>
        <template #port="{ record, rowIndex }">
          <a-input-number v-model="record.port" :min="1" :max="65535" placeholder="端口" style="width: 100%" />
        </template>
        <template #status="{ record }">
          <a-tag v-if="record.status" :color="getDetectionStatusColor(record.status)">
            {{ getDetectionStatusLabel(record.status) }}
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #result="{ record }">
          <a-tag v-if="record.result" :color="getDetectionResultColor(record.result)">
            {{ getDetectionResultLabel(record.result) }}
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #responseTime="{ record }">
          {{ formatResponseTime(record.responseTime) }}
        </template>
        <template #actions="{ record, rowIndex }">
          <a-button type="text" size="small" status="danger" @click="removeBatchTarget(rowIndex)">
            删除
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 统计信息 -->
    <a-card title="检测统计" class="stats-card">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic title="总检测次数" :value="stats.totalDetections" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="在线资产" :value="stats.onlineAssets" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="离线资产" :value="stats.offlineAssets" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="平均响应时间" :value="stats.averageResponseTime" suffix="ms" :precision="2" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlayArrow, IconPlus, IconDelete } from '@arco-design/web-vue/es/icon'
import { 
  assetDetectionApi, 
  detectionUtils,
  type AssetDetection,
  type DetectionType,
  type DetectionStatus,
  type DetectionResult,
  type DetectionStatistics
} from '@/api/assetDetection'

// 响应式数据
const testing = ref(false)
const batchTesting = ref(false)
const testResult = ref<AssetDetection | null>(null)
const stats = ref<DetectionStatistics>({
  totalDetections: 0,
  onlineAssets: 0,
  offlineAssets: 0,
  recentDetections: 0,
  averageResponseTime: 0,
  resultDistribution: {}
})

// 测试表单
const testForm = reactive({
  target: '127.0.0.1',
  port: 80,
  type: 'PING' as DetectionType
})

// 批量测试目标
const batchTargets = ref<any[]>([])

// 批量测试表格列
const batchColumns = [
  { title: '检测类型', slotName: 'type', width: 140 },
  { title: '目标地址', slotName: 'target', width: 200 },
  { title: '端口', slotName: 'port', width: 100 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '结果', slotName: 'result', width: 100 },
  { title: '响应时间', slotName: 'responseTime', width: 100 },
  { title: '操作', slotName: 'actions', width: 80 }
]

// 方法
const runQuickTest = async () => {
  try {
    testing.value = true
    
    // 这里模拟检测过程，实际应该调用后端API
    // 由于我们没有真实的资产ID，这里只是演示
    Message.info('检测功能演示：实际使用时需要先创建资产')
    
    // 模拟检测结果
    testResult.value = {
      id: Date.now(),
      assetId: 1,
      type: testForm.type,
      status: 'COMPLETED' as DetectionStatus,
      result: Math.random() > 0.5 ? 'ONLINE' as DetectionResult : 'OFFLINE' as DetectionResult,
      target: testForm.target,
      port: testForm.port,
      responseTime: Math.floor(Math.random() * 1000) + 10,
      details: '模拟检测结果',
      startTime: new Date().toISOString(),
      createdTime: new Date().toISOString()
    }
    
  } catch (error) {
    Message.error('检测失败')
  } finally {
    testing.value = false
  }
}

const addTestTarget = () => {
  batchTargets.value.push({
    type: 'PING',
    target: '',
    port: 80,
    status: null,
    result: null,
    responseTime: null
  })
}

const removeBatchTarget = (index: number) => {
  batchTargets.value.splice(index, 1)
}

const clearBatchTargets = () => {
  batchTargets.value = []
}

const runBatchTest = async () => {
  try {
    batchTesting.value = true
    
    // 模拟批量检测
    for (let target of batchTargets.value) {
      if (target.target) {
        target.status = 'RUNNING'
        await new Promise(resolve => setTimeout(resolve, 500)) // 模拟延迟
        
        target.status = 'COMPLETED'
        target.result = Math.random() > 0.3 ? 'ONLINE' : 'OFFLINE'
        target.responseTime = Math.floor(Math.random() * 1000) + 10
      }
    }
    
    Message.success('批量检测完成')
  } catch (error) {
    Message.error('批量检测失败')
  } finally {
    batchTesting.value = false
  }
}

const loadStats = async () => {
  try {
    stats.value = await assetDetectionApi.getDetectionStatistics()
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 工具方法
const getDetectionTypeLabel = (type: DetectionType) => {
  return detectionUtils.getDetectionTypeLabel(type)
}

const getDetectionStatusLabel = (status: DetectionStatus) => {
  return detectionUtils.getDetectionStatusLabel(status)
}

const getDetectionStatusColor = (status: DetectionStatus) => {
  const colors = {
    PENDING: 'blue',
    RUNNING: 'orange',
    COMPLETED: 'green',
    FAILED: 'red',
    TIMEOUT: 'orange',
    CANCELLED: 'gray'
  }
  return colors[status] || 'gray'
}

const getDetectionResultLabel = (result: DetectionResult) => {
  return detectionUtils.getDetectionResultLabel(result)
}

const getDetectionResultColor = (result: DetectionResult) => {
  return detectionUtils.getDetectionResultColor(result)
}

const formatResponseTime = (responseTime?: number) => {
  return detectionUtils.formatResponseTime(responseTime)
}

// 生命周期
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.detection-test-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 5px 0 0 0;
  color: #666;
}

.test-card, .result-card, .batch-test-card, .stats-card {
  margin-bottom: 20px;
}

.batch-controls {
  margin-bottom: 16px;
}

.batch-table {
  margin-top: 16px;
}

pre {
  background-color: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
