<template>
  <div class="project-view project-view-enhanced">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header page-header-enhanced">
      <div class="header-left">
        <div class="header-title">
          <icon-folder class="title-icon" />
          <h1 class="gradient-text">项目管理</h1>
        </div>
        <p class="header-subtitle">管理和监控所有安全项目</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="showCreateModal" class="btn-enhanced">
            <template #icon><icon-plus /></template>
            新建项目
          </a-button>
          <a-button @click="showImportModal" class="btn-enhanced">
            <template #icon><icon-upload /></template>
            批量导入
          </a-button>
          <a-button @click="handleExport" class="btn-enhanced">
            <template #icon><icon-download /></template>
            批量导出
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards stats-cards-enhanced">
      <a-row :gutter="24">
        <a-col :span="6">
          <div class="stat-card-enhanced project-total-card">
            <div class="stat-icon project-total-icon">
              <icon-folder />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.total }}</div>
              <div class="stat-title">总项目数</div>
            </div>
            <div class="stat-trend">
              <icon-arrow-up class="trend-up" />
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced project-active-card">
            <div class="stat-icon project-active-icon">
              <icon-play-arrow />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.active }}</div>
              <div class="stat-title">活跃项目</div>
            </div>
            <div class="stat-trend">
              <icon-arrow-up class="trend-up" />
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced project-completed-card">
            <div class="stat-icon project-completed-icon">
              <icon-check-circle />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.completed }}</div>
              <div class="stat-title">已完成</div>
            </div>
            <div class="stat-trend">
              <icon-arrow-up class="trend-up" />
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced project-priority-card">
            <div class="stat-icon project-priority-icon">
              <icon-exclamation />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.high + stats.critical }}</div>
              <div class="stat-title">高优先级</div>
            </div>
            <div class="stat-trend">
              <icon-arrow-up class="trend-up" />
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-card-enhanced">
      <div class="search-header">
        <icon-search class="search-icon" />
        <span class="search-title">搜索筛选</span>
      </div>
      <a-form :model="searchForm" layout="inline" class="search-form-enhanced">
        <a-form-item label="项目名称" class="form-item-enhanced">
          <a-input v-model="searchForm.name" placeholder="请输入项目名称" allow-clear class="input-enhanced" />
        </a-form-item>
        <a-form-item label="状态" class="form-item-enhanced">
          <a-select v-model="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px" class="select-enhanced">
            <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" class="btn-enhanced">
              <icon-search />
              搜索
            </a-button>
            <a-button @click="handleReset" class="btn-enhanced">
              <icon-refresh />
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 项目列表 -->
    <div class="table-card-enhanced">
      <div class="table-header">
        <div class="table-title">
          <icon-list class="table-title-icon" />
          项目列表
        </div>
        <div class="table-info">
          共 {{ pagination.total }} 个项目
        </div>
      </div>
      <a-table 
        :columns="columns" 
        :data="projects" 
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        class="table-enhanced"
      >
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template #priority="{ record }">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ getPriorityLabel(record.priority) }}
          </a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="showDetailModal(record)">详情</a-button>
            <a-button type="text" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-button type="text" size="small" status="danger" @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 创建/编辑项目模态框 -->
    <a-modal 
      v-model:visible="modalVisible" 
      :title="modalTitle"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-form-item label="项目名称" field="name">
          <a-input v-model="formData.name" placeholder="请输入项目名称" />
        </a-form-item>
        <a-form-item label="项目描述" field="description">
          <a-textarea v-model="formData.description" placeholder="请输入项目描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 项目详情模态框 -->
    <a-modal 
      v-model:visible="detailModalVisible" 
      title="项目详情"
      width="600px"
      :footer="false"
    >
      <a-descriptions v-if="selectedProject" :column="2" bordered>
        <a-descriptions-item label="项目名称">{{ selectedProject.name }}</a-descriptions-item>
        <a-descriptions-item label="项目描述">{{ selectedProject.description || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { 
  IconPlus, 
  IconUpload, 
  IconDownload, 
  IconFolder,
  IconPlayArrow,
  IconCheckCircle,
  IconExclamation,
  IconArrowUp,
  IconSearch,
  IconRefresh,
  IconList
} from '@arco-design/web-vue/es/icon'

// 响应式数据
const projects = ref([])
const stats = ref({
  total: 0,
  active: 0,
  completed: 0,
  archived: 0,
  high: 0,
  critical: 0
})
const loading = ref(false)
const modalVisible = ref(false)
const detailModalVisible = ref(false)
const selectedProject = ref(null)
const editingId = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// 表单数据
const formData = reactive({
  name: '',
  description: ''
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入项目名称' }]
}

// 状态选项
const statusOptions = [
  { value: 'ACTIVE', label: '活跃' },
  { value: 'COMPLETED', label: '已完成' }
]

// 表格列配置
const columns = [
  { title: '项目名称', dataIndex: 'name', width: 200 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '优先级', slotName: 'priority', width: 100 },
  { title: '操作', slotName: 'actions', width: 200, fixed: 'right' }
]

// 计算属性
const modalTitle = computed(() => editingId.value ? '编辑项目' : '新建项目')

// 方法
const showCreateModal = () => {
  editingId.value = null
  modalVisible.value = true
}

const showEditModal = (project) => {
  editingId.value = project.id
  modalVisible.value = true
}

const showDetailModal = (project) => {
  selectedProject.value = project
  detailModalVisible.value = true
}

const showImportModal = () => {
  console.log('导入')
}

const handleExport = () => {
  console.log('导出')
}

const handleSearch = () => {
  console.log('搜索')
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
}

const handlePageChange = (page) => {
  pagination.current = page
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
}

const handleModalOk = () => {
  modalVisible.value = false
}

const handleModalCancel = () => {
  modalVisible.value = false
}

const handleDelete = (project) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除项目"${project.name}"吗？`,
    onOk: () => {
      Message.success('删除成功')
    }
  })
}

const getStatusColor = (status) => {
  const colors = {
    ACTIVE: 'green',
    COMPLETED: 'blue'
  }
  return colors[status] || 'gray'
}

const getStatusLabel = (status) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option?.label || status
}

const getPriorityColor = (priority) => {
  const colors = {
    LOW: 'green',
    HIGH: 'orange'
  }
  return colors[priority] || 'gray'
}

const getPriorityLabel = (priority) => {
  return priority || '-'
}

// 生命周期
onMounted(() => {
  // 模拟数据
  projects.value = [
    { id: 1, name: '测试项目1', status: 'ACTIVE', priority: 'HIGH' },
    { id: 2, name: '测试项目2', status: 'COMPLETED', priority: 'LOW' }
  ]
  stats.value = {
    total: 2,
    active: 1,
    completed: 1,
    archived: 0,
    high: 1,
    critical: 0
  }
  pagination.total = 2
})
</script>

<style scoped>
/* 增强样式 */
.project-view-enhanced {
  padding: 0;
  background: var(--bg-secondary);
}

.page-header-enhanced {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.page-header-enhanced::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.title-icon {
  font-size: 32px;
  color: var(--primary-color);
  animation: pulse 2s infinite;
}

.page-header-enhanced h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.header-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

/* 统计卡片增强样式 */
.stats-cards-enhanced {
  margin-bottom: var(--spacing-xl);
}

.project-total-icon {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
}

.project-active-icon {
  background: linear-gradient(135deg, var(--status-success), var(--security-low));
}

.project-completed-icon {
  background: linear-gradient(135deg, var(--security-info), var(--primary-light));
}

.project-priority-icon {
  background: linear-gradient(135deg, var(--security-high), var(--security-critical));
}

/* 搜索卡片增强样式 */
.search-card-enhanced {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.search-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.search-icon {
  font-size: 18px;
  color: var(--primary-color);
}

.search-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.search-form-enhanced {
  margin: 0;
}

/* 表格卡片增强样式 */
.table-card-enhanced {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.table-header {
  padding: var(--spacing-lg);
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.table-title-icon {
  font-size: 18px;
  color: var(--primary-color);
}

.table-info {
  font-size: 14px;
  color: var(--text-secondary);
}

.table-enhanced {
  border-radius: 0 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header-enhanced {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }

  .stats-cards-enhanced .arco-col {
    margin-bottom: var(--spacing-md);
  }

  .search-form-enhanced {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form-enhanced .arco-form-item {
    margin-bottom: var(--spacing-md);
  }
}
</style>
