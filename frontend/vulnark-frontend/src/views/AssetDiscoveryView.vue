<template>
  <div class="asset-discovery-view">
    <!-- 简化的页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>资产发现</h1>
        <p>自动发现网络资产并进行指纹识别</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            新建发现任务
          </a-button>
          <a-button @click="refreshData">
            刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="24">
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.pendingTasks }}</div>
              <div class="stat-title">待执行任务</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.runningTasks }}</div>
              <div class="stat-title">执行中任务</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.completedTasks }}</div>
              <div class="stat-title">已完成任务</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.newDiscoveredAssets }}</div>
              <div class="stat-title">新发现资产</div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 任务列表 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">发现任务列表</div>
        <div class="table-info">共 {{ pagination.total }} 个任务</div>
      </div>
      <a-table
        :columns="columns"
        :data="tasks"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        row-key="id"
      >
      </a-table>
    </div>

    <!-- 创建任务模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      title="新建发现任务"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-form-item label="任务名称" field="name">
          <a-input v-model="formData.name" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="任务描述" field="description">
          <a-textarea v-model="formData.description" placeholder="请输入任务描述" :rows="2" />
        </a-form-item>
        <a-form-item label="扫描目标" field="targets">
          <a-textarea
            v-model="formData.targets"
            :placeholder="getTargetPlaceholder()"
            :rows="4"
          />
          <div class="form-help">
            {{ getTargetHelp() }}
          </div>
        </a-form-item>
      </a-form>
    </a-modal>


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getSystemStatistics, getDiscoveryTasks, createDiscoveryTask, commonPortConfigs } from '@/api/assetDiscovery'
import type { AssetDiscoveryTask, SystemStatistics, AssetDiscoveryTaskRequest } from '@/api/assetDiscovery'

// 响应式数据
const loading = ref(false)
const tasks = ref<AssetDiscoveryTask[]>([])
const systemStats = ref<SystemStatistics>({
  pendingTasks: 0,
  runningTasks: 0,
  completedTasks: 0,
  failedTasks: 0,
  cancelledTasks: 0,
  recentTasks: 0,
  newDiscoveredAssets: 0
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// 模态框状态
const modalVisible = ref(false)
const detailModalVisible = ref(false)
const resultsModalVisible = ref(false)
const isEdit = ref(false)
const selectedTask = ref(null)

// 表单数据
const formData = reactive<AssetDiscoveryTaskRequest>({
  name: '',
  description: '',
  targetType: 'IP_RANGE',
  targets: '',
  scanType: 'PING_SWEEP',
  scanPorts: '',
  scheduleType: 'ONCE'
})

// 表单引用
const formRef = ref()

// 枚举选项
const targetTypeOptions = ref([])
const scanTypeOptions = ref([])
const scheduleTypeOptions = ref([])

// 结果相关
const results = ref([])
const resultsLoading = ref(false)
const taskStats = ref({
  totalResults: 0,
  aliveHosts: 0,
  newAssets: 0,
  matchedAssets: 0,
  updatedAssets: 0,
  ignoredAssets: 0
})

const resultsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入任务名称' }],
  targetType: [{ required: true, message: '请选择目标类型' }],
  targets: [{ required: true, message: '请输入扫描目标' }],
  scanType: [{ required: true, message: '请选择扫描类型' }],
  scheduleType: [{ required: true, message: '请选择调度类型' }]
}

// 表格列定义
const columns = [
  {
    title: '任务名称',
    dataIndex: 'name',
    width: 200
  },
  {
    title: '目标类型',
    dataIndex: 'targetType',
    width: 100
  },
  {
    title: '扫描类型',
    dataIndex: 'scanType',
    width: 100
  },
  {
    title: '任务状态',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
    width: 150
  }
]

// 生命周期
onMounted(() => {
  console.log('Asset Discovery page mounted')
  loadData()
})

// API调用方法
const loadData = async () => {
  await Promise.all([
    loadTasks(),
    loadSystemStats()
  ])
}

const loadTasks = async () => {
  try {
    loading.value = true
    const response = await getDiscoveryTasks(pagination.current - 1, pagination.pageSize)
    tasks.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    console.error('加载任务列表失败:', error)
    Message.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const loadSystemStats = async () => {
  try {
    const response = await getSystemStatistics()
    systemStats.value = response.data
  } catch (error) {
    console.error('加载系统统计失败:', error)
  }
}

// 简化的方法
const showCreateModal = () => {
  modalVisible.value = true
}

const refreshData = () => {
  loadData()
}

// 分页处理
const handlePageChange = (page: number) => {
  pagination.current = page
  console.log('Page changed to:', page)
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  console.log('Page size changed to:', pageSize)
}

// 模态框方法
const handleModalOk = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return

    await createDiscoveryTask(formData)
    Message.success('创建任务成功')
    modalVisible.value = false
    loadTasks()
  } catch (error) {
    console.error('保存任务失败:', error)
    Message.error('保存任务失败')
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    targetType: 'IP_RANGE',
    targets: '',
    scanType: 'PING_SWEEP',
    scanPorts: '',
    scheduleType: 'ONCE'
  })
  formRef.value?.clearValidate()
}

// 简化的工具方法
const getTargetPlaceholder = () => {
  const examples: Record<string, string> = {
    IP_RANGE: '例如: 192.168.1.1-192.168.1.100',
    SUBNET: '例如: 192.168.1.0/24',
    DOMAIN: '例如: example.com',
    URL_LIST: '例如: http://example1.com (每行一个)',
    CUSTOM: '例如: ["192.168.1.1", "192.168.1.2"]'
  }
  return examples[formData.targetType] || '请输入扫描目标'
}

const getTargetHelp = () => {
  const helps: Record<string, string> = {
    IP_RANGE: 'IP范围格式: 起始IP-结束IP',
    SUBNET: 'CIDR格式: 网络地址/子网掩码位数',
    DOMAIN: '域名格式: 单个域名',
    URL_LIST: 'URL列表: 每行一个URL',
    CUSTOM: 'JSON格式: 自定义目标列表'
  }
  return helps[formData.targetType] || ''
}

const handleTargetTypeChange = () => {
  console.log('Target type changed to:', formData.targetType)
}
</script>

<style scoped>
.asset-discovery-view {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left h1 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-title {
  color: #6b7280;
  font-size: 14px;
}

/* 表格卡片样式 */
.table-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.table-info {
  color: #6b7280;
  font-size: 14px;
}

/* 表单样式 */
.form-help {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .asset-discovery-view {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
  }
}
</style>
