<template>
  <div class="scan-result-table">
    <a-table 
      :data="results" 
      :columns="columns"
      :pagination="{ pageSize: 10 }"
      :scroll="{ x: 1200 }"
      size="small"
    >
      <template #severity="{ record }">
        <a-tag :color="getSeverityColor(record.severity)">
          {{ getSeverityText(record.severity) }}
        </a-tag>
      </template>
      
      <template #actions="{ record }">
        <a-button 
          type="text" 
          size="small" 
          @click="viewDetail(record)"
        >
          查看详情
        </a-button>
      </template>
    </a-table>

    <!-- 详情模态框 -->
    <a-modal
      v-model:visible="showDetailModal"
      title="漏洞详情"
      width="800px"
      :footer="false"
    >
      <div v-if="selectedResult" class="result-detail">
        <a-descriptions :data="detailData" :column="1" />
        
        <div v-if="selectedResult.payload" class="payload-section">
          <h4>Payload</h4>
          <a-textarea 
            :model-value="selectedResult.payload" 
            :auto-size="{ minRows: 3, maxRows: 8 }"
            readonly
          />
        </div>
        
        <div v-if="selectedResult.solution" class="solution-section">
          <h4>解决方案</h4>
          <div class="solution-content">{{ selectedResult.solution }}</div>
        </div>
        
        <div v-if="selectedResult.reference" class="reference-section">
          <h4>参考链接</h4>
          <a :href="selectedResult.reference" target="_blank">{{ selectedResult.reference }}</a>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'

// 定义props
interface ScanResult {
  id: number
  scanId: number
  toolName: string
  vulnType: string
  title: string
  url: string
  severity: string
  description: string
  payload?: string
  solution?: string
  reference?: string
  createdTime: string
}

interface Props {
  results: ScanResult[]
}

const props = defineProps<Props>()

// 响应式数据
const showDetailModal = ref(false)
const selectedResult = ref<ScanResult | null>(null)

// 表格列配置
const columns = [
  {
    title: '漏洞标题',
    dataIndex: 'title',
    width: 200,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '工具',
    dataIndex: 'toolName',
    width: 80
  },
  {
    title: '类型',
    dataIndex: 'vulnType',
    width: 120,
    ellipsis: true
  },
  {
    title: '目标URL',
    dataIndex: 'url',
    width: 250,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    slotName: 'severity',
    width: 100
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 200,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '发现时间',
    dataIndex: 'createdTime',
    width: 150,
    render: ({ record }: { record: ScanResult }) => {
      return new Date(record.createdTime).toLocaleString()
    }
  },
  {
    title: '操作',
    slotName: 'actions',
    width: 100,
    fixed: 'right'
  }
]

// 详情数据
const detailData = computed(() => {
  if (!selectedResult.value) return []
  
  const result = selectedResult.value
  return [
    { label: '漏洞标题', value: result.title },
    { label: '扫描工具', value: result.toolName },
    { label: '漏洞类型', value: result.vulnType },
    { label: '目标URL', value: result.url },
    { 
      label: '严重程度', 
      value: h('a-tag', { color: getSeverityColor(result.severity) }, getSeverityText(result.severity))
    },
    { label: '描述', value: result.description },
    { label: '发现时间', value: new Date(result.createdTime).toLocaleString() }
  ]
})

// 获取严重程度颜色
const getSeverityColor = (severity: string) => {
  switch (severity) {
    case 'HIGH': return 'red'
    case 'MEDIUM': return 'orange'
    case 'LOW': return 'gold'
    case 'INFO': return 'blue'
    default: return 'gray'
  }
}

// 获取严重程度文本
const getSeverityText = (severity: string) => {
  switch (severity) {
    case 'HIGH': return '高危'
    case 'MEDIUM': return '中危'
    case 'LOW': return '低危'
    case 'INFO': return '信息'
    default: return severity
  }
}

// 查看详情
const viewDetail = (result: ScanResult) => {
  selectedResult.value = result
  showDetailModal.value = true
}
</script>

<style scoped>
.scan-result-table {
  margin-top: 16px;
}

.result-detail {
  padding: 16px 0;
}

.payload-section,
.solution-section,
.reference-section {
  margin-top: 24px;
}

.payload-section h4,
.solution-section h4,
.reference-section h4 {
  margin-bottom: 8px;
  color: #1d2129;
  font-weight: 600;
}

.solution-content {
  padding: 12px;
  background-color: #f7f8fa;
  border-radius: 4px;
  line-height: 1.6;
}

.reference-section a {
  color: #165dff;
  text-decoration: none;
}

.reference-section a:hover {
  text-decoration: underline;
}
</style>
