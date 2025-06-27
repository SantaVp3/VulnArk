<template>
  <a-modal
    v-model:visible="modalVisible"
    title="扫描结果"
    width="1200px"
    :footer="false"
    @cancel="handleCancel"
  >
    <div class="scan-result-modal">
      <!-- 结果统计 -->
      <a-card class="stats-card" :bordered="false">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="漏洞总数" :value="stats.total" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="严重漏洞" :value="stats.critical" :value-style="{ color: '#f53f3f' }" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="高危漏洞" :value="stats.high" :value-style="{ color: '#ff7d00' }" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="中危漏洞" :value="stats.medium" :value-style="{ color: '#f7ba1e' }" />
          </a-col>
        </a-row>
      </a-card>

      <!-- 搜索过滤器 -->
      <a-card class="filter-card" :bordered="false">
        <a-form :model="filterForm" layout="inline">
          <a-form-item label="漏洞名称">
            <a-input 
              v-model="filterForm.vulnerabilityName" 
              placeholder="请输入漏洞名称"
              allow-clear
              style="width: 200px"
              @change="handleFilter"
            />
          </a-form-item>
          
          <a-form-item label="严重程度">
            <a-select 
              v-model="filterForm.severity" 
              placeholder="请选择严重程度"
              allow-clear
              style="width: 150px"
              @change="handleFilter"
            >
              <a-option value="CRITICAL">严重</a-option>
              <a-option value="HIGH">高危</a-option>
              <a-option value="MEDIUM">中危</a-option>
              <a-option value="LOW">低危</a-option>
              <a-option value="INFO">信息</a-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="目标主机">
            <a-input 
              v-model="filterForm.targetHost" 
              placeholder="请输入目标主机"
              allow-clear
              style="width: 150px"
              @change="handleFilter"
            />
          </a-form-item>
          
          <a-form-item>
            <a-button @click="resetFilter">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 扫描结果表格 -->
      <a-card :bordered="false">
        <a-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
          row-key="id"
          :scroll="{ x: 1200 }"
        >
          <!-- 漏洞名称 -->
          <template #vulnerabilityName="{ record }">
            <a-link @click="viewVulnerabilityDetail(record)">
              {{ record.vulnerabilityName }}
            </a-link>
          </template>

          <!-- 严重程度 -->
          <template #severity="{ record }">
            <a-tag :color="getSeverityColor(record.severity)">
              {{ getSeverityDescription(record.severity) }}
            </a-tag>
          </template>

          <!-- CVSS评分 -->
          <template #cvssScore="{ record }">
            <span v-if="record.cvssScore">
              {{ record.cvssScore.toFixed(1) }}
            </span>
            <span v-else class="text-gray">-</span>
          </template>

          <!-- 目标信息 -->
          <template #target="{ record }">
            <div>
              <div>{{ record.targetHost }}</div>
              <div v-if="record.targetPort" class="text-small text-gray">
                端口: {{ record.targetPort }}
              </div>
            </div>
          </template>

          <!-- CVE编号 -->
          <template #cveId="{ record }">
            <a-link 
              v-if="record.cveId" 
              :href="`https://cve.mitre.org/cgi-bin/cvename.cgi?name=${record.cveId}`"
              target="_blank"
            >
              {{ record.cveId }}
            </a-link>
            <span v-else class="text-gray">-</span>
          </template>

          <!-- 发现时间 -->
          <template #discoveredTime="{ record }">
            {{ formatDateTime(record.discoveredTime) }}
          </template>

          <!-- 操作 -->
          <template #actions="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="viewDetail(record)">
                详情
              </a-button>
              <a-button 
                v-if="!record.falsePositive"
                type="text" 
                size="small" 
                status="warning"
                @click="markAsFalsePositive(record)"
              >
                标记误报
              </a-button>
              <a-button 
                v-else
                type="text" 
                size="small" 
                @click="unmarkFalsePositive(record)"
              >
                取消误报
              </a-button>
            </a-space>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 漏洞详情抽屉 -->
    <a-drawer
      v-model:visible="detailDrawerVisible"
      title="漏洞详情"
      width="600px"
      @cancel="closeDetailDrawer"
    >
      <VulnerabilityDetail 
        v-if="selectedResult"
        :vulnerability="selectedResult"
      />
    </a-drawer>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconRefresh } from '@arco-design/web-vue/es/icon'
import { getScanResults, getSeverityColor, type ScanResult } from '@/api/scan'
import VulnerabilityDetail from './VulnerabilityDetail.vue'
import { formatDateTime } from '@/utils/date'

// Props
interface Props {
  visible: boolean
  taskId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  taskId: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 响应式数据
const loading = ref(false)
const tableData = ref<ScanResult[]>([])
const detailDrawerVisible = ref(false)
const selectedResult = ref<ScanResult | null>(null)

// 计算属性
const modalVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 统计数据
const stats = reactive({
  total: 0,
  critical: 0,
  high: 0,
  medium: 0,
  low: 0,
  info: 0
})

// 过滤表单
const filterForm = reactive({
  vulnerabilityName: '',
  severity: undefined as string | undefined,
  targetHost: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: ['10', '20', '50', '100']
})

// 表格列配置
const columns = [
  {
    title: '漏洞名称',
    dataIndex: 'vulnerabilityName',
    slotName: 'vulnerabilityName',
    width: 250
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    slotName: 'severity',
    width: 100
  },
  {
    title: 'CVSS评分',
    dataIndex: 'cvssScore',
    slotName: 'cvssScore',
    width: 100
  },
  {
    title: '目标信息',
    slotName: 'target',
    width: 150
  },
  {
    title: 'CVE编号',
    dataIndex: 'cveId',
    slotName: 'cveId',
    width: 120
  },
  {
    title: '发现时间',
    dataIndex: 'discoveredTime',
    slotName: 'discoveredTime',
    width: 180
  },
  {
    title: '操作',
    slotName: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 方法
const loadData = async () => {
  if (!props.taskId) return

  try {
    loading.value = true
    
    const response = await getScanResults(
      props.taskId,
      pagination.current - 1,
      pagination.pageSize
    )
    
    if (response.code === 200) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0

      // 更新统计数据
      updateStats(response.data.content || [])
    } else {
      Message.error(response.message || '获取扫描结果失败')
    }
  } catch (error) {
    console.error('获取扫描结果失败:', error)
    Message.error('获取扫描结果失败')
  } finally {
    loading.value = false
  }
}

const updateStats = (results: ScanResult[]) => {
  stats.total = results.length
  stats.critical = results.filter(r => r.severity === 'CRITICAL').length
  stats.high = results.filter(r => r.severity === 'HIGH').length
  stats.medium = results.filter(r => r.severity === 'MEDIUM').length
  stats.low = results.filter(r => r.severity === 'LOW').length
  stats.info = results.filter(r => r.severity === 'INFO').length
}

const handleFilter = () => {
  pagination.current = 1
  loadData()
}

const resetFilter = () => {
  Object.assign(filterForm, {
    vulnerabilityName: '',
    severity: undefined,
    targetHost: ''
  })
  pagination.current = 1
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadData()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadData()
}

const viewDetail = (result: ScanResult) => {
  selectedResult.value = result
  detailDrawerVisible.value = true
}

const closeDetailDrawer = () => {
  detailDrawerVisible.value = false
  selectedResult.value = null
}

const viewVulnerabilityDetail = (result: ScanResult) => {
  viewDetail(result)
}

const markAsFalsePositive = async (result: ScanResult) => {
  try {
    // TODO: 调用API标记为误报
    Message.success('已标记为误报')
    loadData()
  } catch (error) {
    console.error('标记误报失败:', error)
    Message.error('标记误报失败')
  }
}

const unmarkFalsePositive = async (result: ScanResult) => {
  try {
    // TODO: 调用API取消误报标记
    Message.success('已取消误报标记')
    loadData()
  } catch (error) {
    console.error('取消误报标记失败:', error)
    Message.error('取消误报标记失败')
  }
}

const getSeverityDescription = (severity: string): string => {
  const descMap: Record<string, string> = {
    CRITICAL: '严重',
    HIGH: '高危',
    MEDIUM: '中危',
    LOW: '低危',
    INFO: '信息'
  }
  return descMap[severity] || severity
}

const handleCancel = () => {
  modalVisible.value = false
}

// 监听器
watch(() => props.visible, (visible) => {
  if (visible && props.taskId) {
    loadData()
  }
})

watch(() => props.taskId, (taskId) => {
  if (taskId && props.visible) {
    loadData()
  }
})
</script>

<style scoped>
.scan-result-modal {
  max-height: 80vh;
  overflow-y: auto;
}

.stats-card {
  margin-bottom: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.text-gray {
  color: #86909c;
}

.text-small {
  font-size: 12px;
}
</style>
