<template>
  <div class="scan-management">
    <a-card title="扫描任务管理" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <template #icon><PlusOutlined /></template>
            创建扫描任务
          </a-button>
          <a-button @click="refreshTasks">
            <template #icon><ReloadOutlined /></template>
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
        :data-source="tasks"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeText(record.type) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ getPriorityText(record.priority) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'progress'">
            <a-progress :percent="record.progress" size="small" />
          </template>
          
          <template v-if="column.key === 'action'">
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
                danger 
                @click="cancelTask(record.id)"
              >
                取消
              </a-button>
              <a-popconfirm
                title="确定要删除这个扫描任务吗？"
                @confirm="deleteTask(record.id)"
              >
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建任务模态框 -->
    <a-modal
      v-model:open="createModalVisible"
      title="创建扫描任务"
      @ok="handleCreateTask"
      @cancel="resetCreateForm"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        layout="vertical"
      >
        <a-form-item label="任务名称" name="name">
          <a-input v-model:value="createForm.name" placeholder="请输入任务名称" />
        </a-form-item>
        
        <a-form-item label="扫描类型" name="type">
          <a-select v-model:value="createForm.type" placeholder="请选择扫描类型">
            <a-select-option value="PORT_SCAN">端口扫描</a-select-option>
            <a-select-option value="VULNERABILITY_SCAN">漏洞扫描</a-select-option>
            <a-select-option value="SERVICE_SCAN">服务扫描</a-select-option>
            <a-select-option value="WEB_SCAN">Web扫描</a-select-option>
            <a-select-option value="NETWORK_SCAN">网络扫描</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="目标IP" name="targetIp">
          <a-input v-model:value="createForm.targetIp" placeholder="请输入目标IP地址" />
        </a-form-item>
        
        <a-form-item label="目标端口" name="targetPorts">
          <a-input v-model:value="createForm.targetPorts" placeholder="例如: 80,443,8080 或 1-1000" />
        </a-form-item>
        
        <a-form-item label="所属项目" name="projectId">
          <a-select v-model:value="createForm.projectId" placeholder="请选择项目" allow-clear>
            <a-select-option 
              v-for="project in projects" 
              :key="project.id" 
              :value="project.id"
            >
              {{ project.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 任务详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="扫描任务详情"
      :footer="null"
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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { scanApi, projectApi } from '@/api'

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
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
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

const createRules = {
  name: [{ required: true, message: '请输入任务名称' }],
  type: [{ required: true, message: '请选择扫描类型' }],
  targetIp: [{ required: true, message: '请输入目标IP地址' }]
}

// 表格列定义
const columns = [
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '扫描类型', dataIndex: 'type', key: 'type' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '优先级', dataIndex: 'priority', key: 'priority' },
  { title: '目标IP', dataIndex: 'targetIp', key: 'targetIp' },
  { title: '进度', dataIndex: 'progress', key: 'progress' },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime' },
  { title: '操作', key: 'action', width: 200 }
]

// 方法
const loadTasks = async () => {
  loading.value = true
  try {
    const response = await scanApi.getTasks({
      page: pagination.current - 1,
      size: pagination.pageSize
    })
    
    if (response.code === 200) {
      tasks.value = response.data.content
      pagination.total = response.data.totalElements
    }
  } catch (error) {
    message.error('加载扫描任务失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await scanApi.getStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadProjects = async () => {
  try {
    const response = await projectApi.getAll()
    if (response.code === 200) {
      projects.value = response.data
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

const refreshTasks = () => {
  loadTasks()
  loadStats()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTasks()
}

const showCreateModal = () => {
  createModalVisible.value = true
}

const handleCreateTask = async () => {
  try {
    await createFormRef.value.validate()
    
    const response = await scanApi.createTask(createForm)
    if (response.code === 200) {
      message.success('扫描任务创建成功')
      createModalVisible.value = false
      resetCreateForm()
      refreshTasks()
    } else {
      message.error(response.message || '创建失败')
    }
  } catch (error) {
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

const viewTask = (task) => {
  selectedTask.value = task
  detailModalVisible.value = true
}

const executeTask = async (taskId) => {
  try {
    const response = await scanApi.executeTask(taskId)
    if (response.code === 200) {
      message.success('扫描任务已开始执行')
      refreshTasks()
    } else {
      message.error(response.message || '执行失败')
    }
  } catch (error) {
    message.error('执行扫描任务失败')
  }
}

const cancelTask = async (taskId) => {
  try {
    const response = await scanApi.cancelTask(taskId)
    if (response.code === 200) {
      message.success('扫描任务已取消')
      refreshTasks()
    } else {
      message.error(response.message || '取消失败')
    }
  } catch (error) {
    message.error('取消扫描任务失败')
  }
}

const deleteTask = async (taskId) => {
  try {
    const response = await scanApi.deleteTask(taskId)
    if (response.code === 200) {
      message.success('扫描任务删除成功')
      refreshTasks()
    } else {
      message.error(response.message || '删除失败')
    }
  } catch (error) {
    message.error('删除扫描任务失败')
  }
}

// 辅助方法
const getTypeColor = (type) => {
  const colors = {
    PORT_SCAN: 'blue',
    VULNERABILITY_SCAN: 'red',
    SERVICE_SCAN: 'green',
    WEB_SCAN: 'orange',
    NETWORK_SCAN: 'purple'
  }
  return colors[type] || 'default'
}

const getTypeText = (type) => {
  const texts = {
    PORT_SCAN: '端口扫描',
    VULNERABILITY_SCAN: '漏洞扫描',
    SERVICE_SCAN: '服务扫描',
    WEB_SCAN: 'Web扫描',
    NETWORK_SCAN: '网络扫描'
  }
  return texts[type] || type
}

const getStatusColor = (status) => {
  const colors = {
    PENDING: 'default',
    RUNNING: 'processing',
    COMPLETED: 'success',
    FAILED: 'error',
    CANCELLED: 'warning'
  }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待执行',
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getPriorityColor = (priority) => {
  const colors = {
    LOW: 'default',
    MEDIUM: 'blue',
    HIGH: 'orange',
    URGENT: 'red'
  }
  return colors[priority] || 'default'
}

const getPriorityText = (priority) => {
  const texts = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return texts[priority] || priority
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
