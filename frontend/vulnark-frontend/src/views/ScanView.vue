<template>
  <div class="scan-management">
    <a-card title="扫描任务管理" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <template #icon><icon-plus /></template>
            创建扫描任务
          </a-button>
          <a-button @click="refreshTasks">
            <template #icon><icon-refresh /></template>
            刷新
          </a-button>
        </a-space>
        
        <!-- 统计信息 -->
        <div class="stats-cards">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic title="总任务数" :value="stats.total" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="待执行" :value="stats.pending" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="运行中" :value="stats.running" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="已完成" :value="stats.completed" />
            </a-col>
          </a-row>
        </div>
      </div>

      <!-- 任务列表 -->
      <a-table
        :columns="columns"
        :data="tasks"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #type="{ record }">
          <a-tag :color="getTypeColor(record.type)">
            {{ getTypeText(record.type) }}
          </a-tag>
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #priority="{ record }">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ getPriorityText(record.priority) }}
          </a-tag>
        </template>

        <template #progress="{ record }">
          <a-progress :percent="record.progress" size="small" />
        </template>

        <template #createdTime="{ record }">
          {{ formatDateTime(record.createdTime, 'YYYY-MM-DD HH:mm') }}
        </template>

        <template #action="{ record }">
          <a-space>
            <a-button size="small" @click="viewTask(record)">查看</a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              size="small"
              type="primary"
              @click="executeTask(record.id)"
            >
              执行
            </a-button>
            <a-button
              v-if="record.status === 'RUNNING'"
              size="small"
              status="danger"
              @click="cancelTask(record.id)"
            >
              取消
            </a-button>
            <a-popconfirm
              content="确定要删除这个扫描任务吗？"
              @ok="deleteTask(record.id)"
            >
              <a-button size="small" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 创建任务模态框 -->
    <a-modal
      v-model:visible="createModalVisible"
      title="创建扫描任务"
      @ok="handleCreateTask"
      @cancel="resetCreateForm"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        layout="vertical"
      >
        <a-form-item label="任务名称" field="name" :rules="[{ required: true, message: '请输入任务名称' }]">
          <a-input v-model="createForm.name" placeholder="请输入任务名称" />
        </a-form-item>

        <a-form-item label="扫描类型" field="type" :rules="[{ required: true, message: '请选择扫描类型' }]">
          <a-select v-model="createForm.type" placeholder="请选择扫描类型">
            <a-option value="PORT_SCAN">端口扫描</a-option>
            <a-option value="VULNERABILITY_SCAN">漏洞扫描</a-option>
            <a-option value="SERVICE_SCAN">服务扫描</a-option>
            <a-option value="WEB_SCAN">Web扫描</a-option>
            <a-option value="NETWORK_SCAN">网络扫描</a-option>
          </a-select>
        </a-form-item>

        <a-form-item label="目标IP" field="targetIp" :rules="[{ required: true, message: '请输入目标IP地址' }]">
          <a-input v-model="createForm.targetIp" placeholder="请输入目标IP地址" />
        </a-form-item>

        <a-form-item label="目标端口" field="targetPorts">
          <a-input v-model="createForm.targetPorts" placeholder="例如: 80,443,8080 或 1-1000" />
        </a-form-item>

        <a-form-item label="所属项目" field="projectId">
          <a-select v-model="createForm.projectId" placeholder="请选择项目" allow-clear>
            <a-option
              v-for="project in projects"
              :key="project.id"
              :value="project.id"
            >
              {{ project.name }}
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 任务详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="扫描任务详情"
      :footer="false"
      width="800px"
    >
      <div v-if="selectedTask">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="任务名称">{{ selectedTask.name }}</a-descriptions-item>
          <a-descriptions-item label="扫描类型">
            <a-tag :color="getTypeColor(selectedTask.type)">
              {{ getTypeText(selectedTask.type) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedTask.status)">
              {{ getStatusText(selectedTask.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="优先级">
            <a-tag :color="getPriorityColor(selectedTask.priority)">
              {{ getPriorityText(selectedTask.priority) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="目标IP">{{ selectedTask.targetIp }}</a-descriptions-item>
          <a-descriptions-item label="目标端口">{{ selectedTask.targetPorts }}</a-descriptions-item>
          <a-descriptions-item label="进度">
            <a-progress :percent="selectedTask.progress" />
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ selectedTask.createdTime }}</a-descriptions-item>
          <a-descriptions-item label="开始时间">{{ selectedTask.startTime || '未开始' }}</a-descriptions-item>
          <a-descriptions-item label="结束时间">{{ selectedTask.endTime || '未结束' }}</a-descriptions-item>
        </a-descriptions>
        
        <div v-if="selectedTask.scanResult" style="margin-top: 16px;">
          <h4>扫描结果</h4>
          <a-textarea 
            :value="selectedTask.scanResult" 
            :rows="10" 
            readonly 
            style="font-family: monospace;"
          />
        </div>
        
        <div v-if="selectedTask.errorMessage" style="margin-top: 16px;">
          <h4>错误信息</h4>
          <a-alert :message="selectedTask.errorMessage" type="error" />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus, IconRefresh } from '@arco-design/web-vue/es/icon'
import { scanApi } from '@/api/scan'
import { getProjects } from '@/api/project'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const tasks = ref([])
const projects = ref([])
const stats = ref({
  total: 0,
  pending: 0,
  running: 0,
  completed: 0,
  failed: 0
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: number[]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 模态框
const createModalVisible = ref(false)
const detailModalVisible = ref(false)
const selectedTask = ref(null)

// 表单
const createFormRef = ref()
const createForm = reactive({
  name: '',
  type: '',
  targetIp: '',
  targetPorts: '',
  projectId: null
})



// 表格列定义
const columns = [
  { title: '任务名称', dataIndex: 'name' },
  { title: '扫描类型', slotName: 'type' },
  { title: '状态', slotName: 'status' },
  { title: '优先级', slotName: 'priority' },
  { title: '目标IP', dataIndex: 'targetIp' },
  { title: '进度', slotName: 'progress' },
  { title: '创建时间', slotName: 'createdTime' },
  { title: '操作', slotName: 'action', width: 200 }
]

// 方法
const loadTasks = async () => {
  loading.value = true
  try {
    // 模拟数据，实际项目中应该调用真实API
    const mockTasks = [
      {
        id: 1,
        name: '端口扫描任务-192.168.1.100',
        type: 'PORT_SCAN',
        status: 'COMPLETED',
        priority: 'MEDIUM',
        targetIp: '192.168.1.100',
        targetPorts: '1-1000',
        progress: 100,
        createdTime: '2025-06-23 10:00:00',
        startTime: '2025-06-23 10:01:00',
        endTime: '2025-06-23 10:05:00',
        projectId: 1
      },
      {
        id: 2,
        name: 'Web漏洞扫描-example.com',
        type: 'WEB_SCAN',
        status: 'RUNNING',
        priority: 'HIGH',
        targetIp: '192.168.1.200',
        progress: 65,
        createdTime: '2025-06-23 14:30:00',
        startTime: '2025-06-23 14:31:00',
        projectId: 1
      }
    ]

    tasks.value = mockTasks
    pagination.total = mockTasks.length
  } catch (error) {
    Message.error('加载扫描任务失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    // 模拟统计数据
    stats.value = {
      total: 5,
      pending: 1,
      running: 1,
      completed: 2,
      failed: 1
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadProjects = async () => {
  try {
    // 模拟项目数据
    projects.value = [
      { id: 1, name: '企业安全评估项目' },
      { id: 2, name: 'Web应用安全测试' }
    ]
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

const refreshTasks = () => {
  loadTasks()
  loadStats()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadTasks()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadTasks()
}

const showCreateModal = () => {
  createModalVisible.value = true
}

const handleCreateTask = async () => {
  try {
    await createFormRef.value.validate()

    const response = await scanApi.createTask(createForm)
    Message.success('扫描任务创建成功')
    createModalVisible.value = false
    resetCreateForm()
    refreshTasks()
  } catch (error) {
    Message.error('创建扫描任务失败')
    console.error('创建扫描任务失败:', error)
  }
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    name: '',
    type: '',
    targetIp: '',
    targetPorts: '',
    projectId: null
  })
  createFormRef.value?.resetFields()
}

const viewTask = (task: any) => {
  selectedTask.value = task
  detailModalVisible.value = true
}

const executeTask = async (taskId: number) => {
  try {
    await scanApi.executeTask(taskId)
    Message.success('扫描任务已开始执行')
    refreshTasks()
  } catch (error) {
    Message.error('执行扫描任务失败')
  }
}

const cancelTask = async (taskId: number) => {
  try {
    await scanApi.cancelTask(taskId)
    Message.success('扫描任务已取消')
    refreshTasks()
  } catch (error) {
    Message.error('取消扫描任务失败')
  }
}

const deleteTask = async (taskId: number) => {
  try {
    await scanApi.deleteTask(taskId)
    Message.success('扫描任务删除成功')
    refreshTasks()
  } catch (error) {
    Message.error('删除扫描任务失败')
  }
}

// 辅助方法
const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    PORT_SCAN: 'blue',
    VULNERABILITY_SCAN: 'red',
    SERVICE_SCAN: 'green',
    WEB_SCAN: 'orange',
    NETWORK_SCAN: 'purple'
  }
  return colors[type] || 'default'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    PORT_SCAN: '端口扫描',
    VULNERABILITY_SCAN: '漏洞扫描',
    SERVICE_SCAN: '服务扫描',
    WEB_SCAN: 'Web扫描',
    NETWORK_SCAN: '网络扫描'
  }
  return texts[type] || type
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    PENDING: 'default',
    RUNNING: 'processing',
    COMPLETED: 'success',
    FAILED: 'error',
    CANCELLED: 'warning'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待执行',
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getPriorityColor = (priority: string) => {
  const colors: Record<string, string> = {
    LOW: 'default',
    MEDIUM: 'blue',
    HIGH: 'orange',
    URGENT: 'red'
  }
  return colors[priority] || 'default'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return texts[priority] || priority
}

const getProjectName = (projectId: number) => {
  const project = projects.value.find(p => p.id === projectId)
  return project?.name || '未知项目'
}

// 生命周期
onMounted(() => {
  loadTasks()
  loadStats()
  loadProjects()
})
</script>

<style scoped>
.scan-management {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
}

.stats-cards {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}
</style>
