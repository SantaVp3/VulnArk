<template>
  <div class="scan-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">扫描管理</h1>
        <p class="page-description">管理和执行安全扫描任务</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button @click="showConfigManager">
            <template #icon>
              <icon-settings />
            </template>
            配置管理
          </a-button>
          <a-button @click="refreshData">
            <template #icon>
              <icon-refresh />
            </template>
            刷新
          </a-button>
          <a-button type="primary" @click="showCreateModal">
            <template #icon>
              <icon-plus />
            </template>
            新建扫描
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic 
              title="总扫描数" 
              :value="stats.total"
              :loading="statsLoading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic 
              title="运行中" 
              :value="stats.running" 
              value-style="color: #3b82f6"
              :loading="statsLoading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic 
              title="已完成" 
              :value="stats.completed" 
              value-style="color: #10b981"
              :loading="statsLoading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic 
              title="失败" 
              :value="stats.failed" 
              value-style="color: #ef4444"
              :loading="statsLoading"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 过滤器 -->
    <div class="filter-section">
      <a-card>
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="任务名称">
            <a-input 
              v-model:value="searchForm.name" 
              placeholder="输入任务名称"
              allow-clear
              @change="handleSearch"
            />
          </a-form-item>
          <a-form-item label="状态">
            <a-select 
              v-model:value="searchForm.status" 
              placeholder="选择状态"
              allow-clear
              style="width: 120px"
              @change="handleSearch"
            >
              <a-select-option value="CREATED">已创建</a-select-option>
              <a-select-option value="QUEUED">排队中</a-select-option>
              <a-select-option value="RUNNING">运行中</a-select-option>
              <a-select-option value="PAUSED">已暂停</a-select-option>
              <a-select-option value="COMPLETED">已完成</a-select-option>
              <a-select-option value="FAILED">失败</a-select-option>
              <a-select-option value="CANCELLED">已取消</a-select-option>
              <a-select-option value="TIMEOUT">超时</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="扫描类型">
            <a-select 
              v-model:value="searchForm.scanType" 
              placeholder="选择扫描类型"
              allow-clear
              style="width: 140px"
              @change="handleSearch"
            >
              <a-select-option value="PORT_SCAN">端口扫描</a-select-option>
              <a-select-option value="WEB_SCAN">Web应用扫描</a-select-option>
              <a-select-option value="SYSTEM_SCAN">系统漏洞扫描</a-select-option>
              <a-select-option value="COMPREHENSIVE_SCAN">综合扫描</a-select-option>
              <a-select-option value="CUSTOM_SCAN">自定义扫描</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="扫描引擎">
            <a-select 
              v-model:value="searchForm.scanEngine" 
              placeholder="选择扫描引擎"
              allow-clear
              style="width: 120px"
              @change="handleSearch"
            >
              <a-select-option value="NUCLEI">Nuclei</a-select-option>
              <a-select-option value="AWVS">AWVS</a-select-option>
              <a-select-option value="NESSUS">Nessus</a-select-option>
              <a-select-option value="OPENVAS">OpenVAS</a-select-option>
              <a-select-option value="NMAP">Nmap</a-select-option>
              <a-select-option value="INTERNAL">内置</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button @click="resetSearch">重置</a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 扫描任务表格 -->
    <div class="table-section">
      <a-card>
        <a-table 
          :data-source="scanTasks" 
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          row-key="id"
          :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        >
          <a-table-column title="ID" data-index="id" width="80" />
          <a-table-column title="扫描名称" data-index="name" width="200">
            <template #cell="{ record }">
              <a @click="viewTaskDetails(record)">{{ record.name }}</a>
            </template>
          </a-table-column>
          <a-table-column title="目标" data-index="targets" width="200">
            <template #cell="{ record }">
              <a-tooltip :content="record.targets">
                <span class="text-truncate">{{ record.targets }}</span>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column title="类型" data-index="scanType" width="120">
            <template #cell="{ record }">
              <a-tag>{{ getScanTypeLabel(record.scanType) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="引擎" data-index="scanEngineType" width="100">
            <template #cell="{ record }">
              <a-tag color="blue">{{ record.scanEngineType }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="配置" data-index="scanConfigId" width="120">
            <template #cell="{ record }">
              <span v-if="record.scanConfigId">
                {{ getConfigName(record.scanConfigId) }}
              </span>
              <span v-else class="text-muted">默认配置</span>
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="status" width="100">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusLabel(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="进度" data-index="progress" width="120">
            <template #cell="{ record }">
              <a-progress 
                :percent="record.progress || 0" 
                size="small" 
                :status="getProgressStatus(record.status)"
              />
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createdTime" width="150">
            <template #cell="{ record }">
              {{ formatDate(record.createdTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" width="240" fixed="right">
            <template #cell="{ record }">
              <a-space size="small">
                <a-button type="text" size="small" @click="viewTaskDetails(record)">
                  详情
                </a-button>
                <a-button 
                  v-if="record.status === 'CREATED' || record.status === 'PAUSED'"
                  type="text" 
                  size="small" 
                  @click="startTask(record)"
                  :loading="record.starting"
                >
                  启动
                </a-button>
                <a-button 
                  v-if="record.status === 'RUNNING'"
                  type="text" 
                  size="small" 
                  @click="stopTask(record)"
                  :loading="record.stopping"
                >
                  停止
                </a-button>
                <a-dropdown>
                  <a-button type="text" size="small">
                    更多
                    <icon-down />
                  </a-button>
                  <template #content>
                    <a-doption @click="editTask(record)">编辑</a-doption>
                    <a-doption @click="cloneTask(record)">克隆</a-doption>
                    <a-doption @click="exportResults(record)" :disabled="!record.id">导出结果</a-doption>
                    <a-doption @click="deleteTask(record)" class="text-red">删除</a-doption>
                  </template>
                </a-dropdown>
              </a-space>
            </template>
          </a-table-column>
        </a-table>
      </a-card>
    </div>

    <!-- 创建/编辑扫描任务模态框 -->
    <a-modal
      v-model:visible="createModalVisible"
      :title="isEditing ? '编辑扫描任务' : '创建扫描任务'"
      width="1200px"
      :footer="false"
      @cancel="resetCreateForm"
    >
      <ScanTaskForm
        ref="scanFormRef"
        v-model="formData"
        :available-configs="availableConfigs"
        :config-loading="configLoading"
        @config-change="onConfigChange"
        @open-config-manager="showConfigManager"
      />
      
      <div class="modal-footer">
        <a-space>
          <a-button @click="resetCreateForm">取消</a-button>
          <a-button 
            type="primary" 
            @click="handleSubmit"
            :loading="submitting"
          >
            {{ isEditing ? '更新' : '创建' }}
          </a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 扫描配置管理器 -->
    <ScanConfigManagerModal
      v-model="configManagerVisible"
      @config-created="loadAvailableConfigs"
    />

    <!-- 任务详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="扫描任务详情"
      width="900px"
      :footer="false"
    >
      <div v-if="selectedTask" class="task-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="任务名称">{{ selectedTask.name }}</a-descriptions-item>
          <a-descriptions-item label="扫描类型">{{ getScanTypeLabel(selectedTask.scanType) }}</a-descriptions-item>
          <a-descriptions-item label="扫描引擎">{{ selectedTask.scanEngineType }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedTask.status)">
              {{ getStatusLabel(selectedTask.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="扫描目标" :span="2">{{ selectedTask.targets }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatDate(selectedTask.createdTime) }}</a-descriptions-item>
          <a-descriptions-item label="开始时间">{{ formatDate(selectedTask.startTime) }}</a-descriptions-item>
          <a-descriptions-item label="结束时间">{{ formatDate(selectedTask.endTime) }}</a-descriptions-item>
          <a-descriptions-item label="执行时间">{{ formatDuration(selectedTask.executionTime) }}</a-descriptions-item>
          <a-descriptions-item label="任务描述" :span="2">{{ selectedTask.description || '无' }}</a-descriptions-item>
        </a-descriptions>
        
        <div v-if="selectedTask.scanParameters" class="mt-4">
          <h4>扫描参数</h4>
          <pre class="parameter-text">{{ selectedTask.scanParameters }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { 
  IconSettings, 
  IconRefresh, 
  IconPlus, 
  IconDown,
  IconEye, 
  IconPause, 
  IconStop, 
  IconDelete,
  IconEdit
} from '@arco-design/web-vue/es/icon'
import { scanApi, scanConfigApi, type ScanTask, type ScanTaskRequest, type ScanConfig, getAvailableScanConfigs } from '@/api/scan'
import ScanTaskForm from '@/components/ScanTaskForm.vue'
import ScanConfigManagerModal from '@/components/ScanConfigManagerModal.vue'

// 响应式数据
const loading = ref(false)
const statsLoading = ref(false)
const submitting = ref(false)
const configLoading = ref(false)
const createModalVisible = ref(false)
const configManagerVisible = ref(false)
const detailModalVisible = ref(false)
const isEditing = ref(false)
const selectedRowKeys = ref<number[]>([])

const scanTasks = ref<ScanTask[]>([])
const availableConfigs = ref<ScanConfig[]>([])
const configNameMap = ref<Record<number, string>>({})
const selectedTask = ref<ScanTask | null>(null)
const scanFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  scanType: '',
  scanEngine: ''
})

// 统计数据
const stats = reactive({
  total: 0,
  running: 0,
  completed: 0,
  failed: 0
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表单数据
const formData = ref<ScanTaskRequest>({
  name: '',
  description: '',
  targets: '',
  scanType: 'PORT_SCAN',
  scanEngineType: 'NUCLEI',
  projectId: 1, // 默认项目ID
  scanConfigId: undefined,
  scanParameters: '',
  customScripts: '',
  options: {}
})

// 初始化默认表单数据
const initFormData = (): ScanTaskRequest => ({
  name: '',
  description: '',
  targets: '',
  scanType: 'PORT_SCAN',
  scanEngineType: 'NUCLEI',
  projectId: 1,
  scanConfigId: undefined,
  scanParameters: '',
  customScripts: '',
  options: {
    timeoutMinutes: 60,
    maxConcurrency: 5,
    scanDepth: 'NORMAL',
    portRange: '1-1000',
    excludePorts: '',
    enableUdpScan: false,
    enableServiceDetection: true,
    enableOsDetection: false,
    enableWebAppScan: false,
    enableVulnerabilityScan: true,
    enableComplianceCheck: false,
    vulnerabilityTypes: [],
    scanSpeed: 'NORMAL',
    retryCount: 2,
    customScript: '',
    customParams: ''
  }
})

// 标签映射函数
const getScanTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'PORT_SCAN': '端口扫描',
    'WEB_SCAN': 'Web应用扫描',
    'SYSTEM_SCAN': '系统漏洞扫描',
    'COMPREHENSIVE_SCAN': '综合扫描',
    'CUSTOM_SCAN': '自定义扫描'
  }
  return labels[type] || type
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'CREATED': '已创建',
    'QUEUED': '排队中',
    'RUNNING': '运行中',
    'PAUSED': '已暂停',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消',
    'TIMEOUT': '超时'
  }
  return labels[status] || status
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'CREATED': 'default',
    'QUEUED': 'orange',
    'RUNNING': 'blue',
    'PAUSED': 'warning',
    'COMPLETED': 'green',
    'FAILED': 'red',
    'CANCELLED': 'default',
    'TIMEOUT': 'red'
  }
  return colors[status] || 'default'
}

const getProgressStatus = (status: string) => {
  if (status === 'FAILED' || status === 'TIMEOUT') return 'exception'
  if (status === 'COMPLETED') return 'success'
  return 'normal'
}

// 获取配置名称
const getConfigName = (configId: number) => {
  return configNameMap.value[configId] || availableConfigs.value.find(c => c.id === configId)?.name || `配置${configId}`
}

// 时间格式化
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const formatDuration = (duration?: number) => {
  if (!duration) return '-'
  const minutes = Math.floor(duration / 60000)
  const seconds = Math.floor((duration % 60000) / 1000)
  return `${minutes}分${seconds}秒`
}

// 数据加载
const loadScanTasks = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (!params[key as keyof typeof params]) {
        delete params[key as keyof typeof params]
      }
    })
    
