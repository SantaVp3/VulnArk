<template>
  <div class="scan-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>漏洞管理</a-breadcrumb-item>
        <a-breadcrumb-item>扫描管理</a-breadcrumb-item>
      </a-breadcrumb>
      
      <div class="header-actions">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <template #icon><icon-plus /></template>
            创建扫描任务
          </a-button>
          <a-button @click="refreshData">
            <template #icon><icon-refresh /></template>
            刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 搜索过滤器 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline" @submit="handleSearch">
        <a-form-item label="任务名称">
          <a-input 
            v-model="searchForm.name" 
            placeholder="请输入任务名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        
        <a-form-item label="扫描类型">
          <a-select 
            v-model="searchForm.scanType" 
            placeholder="请选择扫描类型"
            allow-clear
            style="width: 150px"
          >
            <a-option value="PORT_SCAN">端口扫描</a-option>
            <a-option value="WEB_SCAN">Web应用扫描</a-option>
            <a-option value="SYSTEM_SCAN">系统漏洞扫描</a-option>
            <a-option value="COMPREHENSIVE_SCAN">综合扫描</a-option>
            <a-option value="CUSTOM_SCAN">自定义扫描</a-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="任务状态">
          <a-select 
            v-model="searchForm.status" 
            placeholder="请选择任务状态"
            allow-clear
            style="width: 150px"
          >
            <a-option value="CREATED">已创建</a-option>
            <a-option value="QUEUED">排队中</a-option>
            <a-option value="RUNNING">扫描中</a-option>
            <a-option value="PAUSED">已暂停</a-option>
            <a-option value="COMPLETED">已完成</a-option>
            <a-option value="FAILED">扫描失败</a-option>
            <a-option value="CANCELLED">已取消</a-option>
            <a-option value="TIMEOUT">扫描超时</a-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="扫描引擎">
          <a-select 
            v-model="searchForm.scanEngine" 
            placeholder="请选择扫描引擎"
            allow-clear
            style="width: 150px"
          >
            <a-option value="NESSUS">Nessus</a-option>
            <a-option value="OPENVAS">OpenVAS</a-option>
            <a-option value="AWVS">AWVS</a-option>
            <a-option value="NUCLEI">Nuclei</a-option>
            <a-option value="NMAP">Nmap</a-option>
            <a-option value="INTERNAL">内置引擎</a-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><icon-search /></template>
              搜索
            </a-button>
            <a-button @click="resetSearch">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 扫描任务表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        row-key="id"
      >
        <!-- 任务名称 -->
        <template #name="{ record }">
          <a-link @click="viewTaskDetail(record)">{{ record.name }}</a-link>
        </template>

        <!-- 扫描类型 -->
        <template #scanType="{ record }">
          <a-tag :color="getScanTypeColor(record.scanType)">
            {{ getScanTypeDescription(record.scanType) }}
          </a-tag>
        </template>

        <!-- 扫描引擎 -->
        <template #scanEngine="{ record }">
          <a-tag color="blue">
            {{ getScanEngineDescription(record.scanEngine) }}
          </a-tag>
        </template>

        <!-- 任务状态 -->
        <template #status="{ record }">
          <a-tag :color="getTaskStatusColor(record.status)">
            {{ getTaskStatusDescription(record.status) }}
          </a-tag>
        </template>

        <!-- 进度 -->
        <template #progress="{ record }">
          <a-progress 
            :percent="record.progress" 
            :status="getProgressStatus(record.status)"
            size="small"
          />
        </template>

        <!-- 漏洞统计 -->
        <template #vulnerabilities="{ record }">
          <div v-if="record.totalVulnerabilityCount > 0" class="vulnerability-stats">
            <a-space>
              <a-tag v-if="record.highRiskCount > 0" color="red" size="small">
                高危: {{ record.highRiskCount }}
              </a-tag>
              <a-tag v-if="record.mediumRiskCount > 0" color="orange" size="small">
                中危: {{ record.mediumRiskCount }}
              </a-tag>
              <a-tag v-if="record.lowRiskCount > 0" color="blue" size="small">
                低危: {{ record.lowRiskCount }}
              </a-tag>
              <a-tag v-if="record.infoRiskCount > 0" color="gray" size="small">
                信息: {{ record.infoRiskCount }}
              </a-tag>
            </a-space>
          </div>
          <span v-else class="text-gray">暂无数据</span>
        </template>

        <!-- 创建时间 -->
        <template #createdTime="{ record }">
          {{ formatDateTime(record.createdTime) }}
        </template>

        <!-- 操作 -->
        <template #actions="{ record }">
          <a-space>
            <a-button 
              v-if="canStartTask(record.status)"
              type="primary" 
              size="small"
              @click="startTask(record)"
            >
              启动
            </a-button>
            
            <a-button 
              v-if="canStopTask(record.status)"
              status="warning" 
              size="small"
              @click="stopTask(record)"
            >
              停止
            </a-button>
            
            <a-button 
              v-if="isTaskCompleted(record.status)"
              type="outline" 
              size="small"
              @click="viewResults(record)"
            >
              查看结果
            </a-button>
            
            <a-dropdown>
              <a-button type="text" size="small">
                <template #icon><icon-more /></template>
              </a-button>
              <template #content>
                <a-doption @click="editTask(record)">编辑</a-doption>
                <a-doption @click="duplicateTask(record)">复制</a-doption>
                <a-doption @click="deleteTask(record)" class="danger">删除</a-doption>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑扫描任务模态框 -->
    <CreateScanTaskModal
      v-model:visible="createModalVisible"
      :task-data="editingTask"
      @success="handleCreateSuccess"
    />

    <!-- 扫描结果模态框 -->
    <ScanResultModal
      v-model:visible="resultModalVisible"
      :task-id="selectedTaskId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { 
  IconPlus, 
  IconRefresh, 
  IconSearch, 
  IconMore 
} from '@arco-design/web-vue/es/icon'
import { 
  getScanTasks, 
  searchScanTasks, 
  startScanTask, 
  stopScanTask, 
  deleteScanTask,
  getTaskStatusColor,
  getScanTypeColor,
  getScanTypeDescription,
  getScanEngineDescription,
  getTaskStatusDescription,
  canStartTask,
  canStopTask,
  isTaskCompleted,
  type ScanTask,
  type TaskStatus,
  type ScanType,
  type ScanEngine
} from '@/api/scan'
import CreateScanTaskModal from './components/CreateScanTaskModal.vue'
import ScanResultModal from './components/ScanResultModal.vue'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const tableData = ref<ScanTask[]>([])
const createModalVisible = ref(false)
const resultModalVisible = ref(false)
const editingTask = ref<ScanTask | null>(null)
const selectedTaskId = ref<number | null>(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  scanType: undefined as ScanType | undefined,
  status: undefined as TaskStatus | undefined,
  scanEngine: undefined as ScanEngine | undefined
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
    title: '任务名称',
    dataIndex: 'name',
    slotName: 'name',
    width: 200
  },
  {
    title: '扫描类型',
    dataIndex: 'scanType',
    slotName: 'scanType',
    width: 120
  },
  {
    title: '扫描引擎',
    dataIndex: 'scanEngine',
    slotName: 'scanEngine',
    width: 120
  },
  {
    title: '任务状态',
    dataIndex: 'status',
    slotName: 'status',
    width: 100
  },
  {
    title: '进度',
    dataIndex: 'progress',
    slotName: 'progress',
    width: 120
  },
  {
    title: '漏洞统计',
    slotName: 'vulnerabilities',
    width: 200
  },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
    slotName: 'createdTime',
    width: 180
  },
  {
    title: '操作',
    slotName: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 计算属性
const getProgressStatus = (status: TaskStatus) => {
  if (status === 'FAILED' || status === 'TIMEOUT') return 'danger'
  if (status === 'COMPLETED') return 'success'
  if (status === 'RUNNING') return 'normal'
  return 'normal'
}

// 方法
const loadData = async () => {
  try {
    loading.value = true

    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      ...searchForm
    }

    const response = await searchScanTasks(params)

    // 后端返回格式: { code: 200, message: "...", data: {...} }
    if (response.code === 200) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    } else {
      Message.error(response.message || '获取扫描任务列表失败')
    }
  } catch (error) {
    console.error('获取扫描任务列表失败:', error)
    Message.error('获取扫描任务列表失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    scanType: undefined,
    status: undefined,
    scanEngine: undefined
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

const showCreateModal = () => {
  editingTask.value = null
  createModalVisible.value = true
}

const editTask = (task: ScanTask) => {
  editingTask.value = task
  createModalVisible.value = true
}

const duplicateTask = (task: ScanTask) => {
  const duplicatedTask = { ...task }
  delete duplicatedTask.id
  duplicatedTask.name = `${task.name} - 副本`
  editingTask.value = duplicatedTask as ScanTask
  createModalVisible.value = true
}

const startTask = async (task: ScanTask) => {
  try {
    const response = await startScanTask(task.id)
    if (response.code === 200) {
      Message.success('扫描任务启动成功')
      loadData()
    } else {
      Message.error(response.message || '启动扫描任务失败')
    }
  } catch (error) {
    console.error('启动扫描任务失败:', error)
    Message.error('启动扫描任务失败')
  }
}

const stopTask = async (task: ScanTask) => {
  try {
    const response = await stopScanTask(task.id)
    if (response.code === 200) {
      Message.success('扫描任务停止成功')
      loadData()
    } else {
      Message.error(response.message || '停止扫描任务失败')
    }
  } catch (error) {
    console.error('停止扫描任务失败:', error)
    Message.error('停止扫描任务失败')
  }
}

const deleteTask = (task: ScanTask) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除扫描任务"${task.name}"吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        const response = await deleteScanTask(task.id)
        if (response.code === 200) {
          Message.success('扫描任务删除成功')
          loadData()
        } else {
          Message.error(response.message || '删除扫描任务失败')
        }
      } catch (error) {
        console.error('删除扫描任务失败:', error)
        Message.error('删除扫描任务失败')
      }
    }
  })
}

const viewTaskDetail = (task: ScanTask) => {
  // TODO: 跳转到任务详情页面
  console.log('查看任务详情:', task)
}

const viewResults = (task: ScanTask) => {
  selectedTaskId.value = task.id
  resultModalVisible.value = true
}

const handleCreateSuccess = () => {
  createModalVisible.value = false
  loadData()
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.scan-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.vulnerability-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.text-gray {
  color: #86909c;
}

.danger {
  color: #f53f3f;
}
</style>
