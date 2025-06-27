<template>
  <div class="baseline-check-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>安全管理</a-breadcrumb-item>
        <a-breadcrumb-item>基线检查</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">基线检查管理</h1>
      <p class="page-description">对系统、网络、数据库等进行安全基线配置检查</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="总检查数"
              :value="statistics.totalChecks"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <icon-file-text />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="运行中"
              :value="statistics.runningChecks"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <icon-play-circle />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="已完成"
              :value="statistics.completedChecks"
              :value-style="{ color: '#722ed1' }"
            >
              <template #prefix>
                <icon-check-circle />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="平均合规分数"
              :value="statistics.averageScore"
              suffix="%"
              :precision="1"
              :value-style="{ color: '#fa8c16' }"
            >
              <template #prefix>
                <icon-trophy />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <a-button type="primary" @click="handleCreateCheck">
          <template #icon>
            <icon-plus />
          </template>
          新建检查
        </a-button>
        <a-button @click="refreshData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
      </div>
      <div class="action-right">
        <a-input-search
          v-model="searchForm.name"
          placeholder="搜索检查名称"
          style="width: 200px"
          @search="handleSearch"
        />
        <a-select
          v-model="searchForm.checkType"
          placeholder="检查类型"
          style="width: 150px; margin-left: 8px"
          allow-clear
          @change="handleSearch"
        >
          <a-option
            v-for="type in checkTypes"
            :key="type.value"
            :value="type.value"
          >
            {{ type.label }}
          </a-option>
        </a-select>
        <a-select
          v-model="searchForm.status"
          placeholder="检查状态"
          style="width: 120px; margin-left: 8px"
          allow-clear
          @change="handleSearch"
        >
          <a-option
            v-for="status in checkStatuses"
            :key="status.value"
            :value="status.value"
          >
            {{ status.label }}
          </a-option>
        </a-select>
      </div>
    </div>

    <!-- 检查列表 -->
    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        row-key="id"
      >
        <!-- 检查类型 -->
        <template #checkType="{ record }">
          <a-tag :color="getCheckTypeColor(record.checkType)">
            {{ getCheckTypeLabel(record.checkType) }}
          </a-tag>
        </template>

        <!-- 检查状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            <icon-loading v-if="record.status === 'RUNNING'" spin />
            {{ getStatusLabel(record.status) }}
          </a-tag>
        </template>

        <!-- 检查结果 -->
        <template #result="{ record }">
          <a-tag
            v-if="record.result"
            :color="getResultColor(record.result)"
          >
            {{ getResultLabel(record.result) }}
          </a-tag>
          <span v-else>-</span>
        </template>

        <!-- 进度 -->
        <template #progress="{ record }">
          <a-progress
            :percent="record.progress"
            :size="'small'"
            :status="getProgressStatus(record.status)"
          />
        </template>

        <!-- 合规分数 -->
        <template #complianceScore="{ record }">
          <span :class="getScoreClass(record.complianceScore)">
            {{ record.complianceScore?.toFixed(1) || 0 }}%
          </span>
        </template>

        <!-- 资产 -->
        <template #asset="{ record }">
          <span v-if="record.asset">
            {{ record.asset.name }}
          </span>
          <span v-else>-</span>
        </template>

        <!-- 创建时间 -->
        <template #createdTime="{ record }">
          {{ formatDateTime(record.createdTime) }}
        </template>

        <!-- 操作 -->
        <template #actions="{ record }">
          <a-space>
            <a-button
              type="text"
              size="small"
              @click="viewDetails(record)"
            >
              详情
            </a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              type="text"
              size="small"
              status="success"
              @click="startCheck(record)"
            >
              启动
            </a-button>
            <a-button
              v-if="record.status === 'RUNNING'"
              type="text"
              size="small"
              status="warning"
              @click="stopCheck(record)"
            >
              停止
            </a-button>
            <a-button
              v-if="record.status === 'COMPLETED'"
              type="text"
              size="small"
              @click="viewReport(record)"
            >
              报告
            </a-button>
            <a-popconfirm
              content="确定要删除这个基线检查吗？"
              @ok="deleteCheck(record)"
            >
              <a-button
                type="text"
                size="small"
                status="danger"
              >
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 创建检查模态框 -->
    <!-- <CreateBaselineCheckModal
      v-model:visible="createModalVisible"
      @success="handleCreateSuccess"
    /> -->

    <!-- 检查详情模态框 -->
    <!-- <BaselineCheckDetailModal
      v-model:visible="detailModalVisible"
      :check-id="selectedCheckId"
    /> -->

    <!-- 检查报告模态框 -->
    <!-- <BaselineCheckReportModal
      v-model:visible="reportModalVisible"
      :check-id="selectedCheckId"
    /> -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconPlus,
  IconRefresh,
  IconFileText,
  IconPlayCircle,
  IconCheckCircle,
  IconTrophy,
  IconLoading
} from '@arco-design/web-vue/es/icon'
import {
  searchBaselineChecks,
  startBaselineCheck,
  stopBaselineCheck,
  deleteBaselineCheck,
  getCheckStatistics,
  type BaselineCheck,
  type BaselineCheckSearchParams,
  CheckType,
  CheckStatus,
  CheckResult
} from '@/api/baseline'
import { formatDateTime } from '@/utils/date'
// import CreateBaselineCheckModal from '@/components/baseline/CreateBaselineCheckModal.vue'
// import BaselineCheckDetailModal from '@/components/baseline/BaselineCheckDetailModal.vue'
// import BaselineCheckReportModal from '@/components/baseline/BaselineCheckReportModal.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<BaselineCheck[]>([])
const createModalVisible = ref(false)
const detailModalVisible = ref(false)
const reportModalVisible = ref(false)
const selectedCheckId = ref<number>()

// 搜索表单
const searchForm = reactive<BaselineCheckSearchParams>({
  name: '',
  checkType: undefined,
  status: undefined,
  page: 0,
  size: 10,
  sortBy: 'createdTime',
  sortDir: 'desc'
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: true,
  pageSizeOptions: ['10', '20', '50', '100']
})

// 统计数据
const statistics = reactive({
  totalChecks: 0,
  runningChecks: 0,
  completedChecks: 0,
  averageScore: 0
})

// 表格列配置
const columns = [
  {
    title: '检查名称',
    dataIndex: 'name',
    width: 200,
    ellipsis: true,
    tooltip: true
  },
  {
    title: '检查类型',
    dataIndex: 'checkType',
    slotName: 'checkType',
    width: 120
  },
  {
    title: '目标资产',
    dataIndex: 'asset',
    slotName: 'asset',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    slotName: 'status',
    width: 100
  },
  {
    title: '结果',
    dataIndex: 'result',
    slotName: 'result',
    width: 80
  },
  {
    title: '进度',
    dataIndex: 'progress',
    slotName: 'progress',
    width: 120
  },
  {
    title: '合规分数',
    dataIndex: 'complianceScore',
    slotName: 'complianceScore',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdTime',
    slotName: 'createdTime',
    width: 160
  },
  {
    title: '操作',
    slotName: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 检查类型选项
const checkTypes = [
  { value: CheckType.SYSTEM_SECURITY, label: '系统安全基线' },
  { value: CheckType.NETWORK_SECURITY, label: '网络安全基线' },
  { value: CheckType.DATABASE_SECURITY, label: '数据库安全基线' },
  { value: CheckType.WEB_SECURITY, label: 'Web应用安全基线' },
  { value: CheckType.MIDDLEWARE_SECURITY, label: '中间件安全基线' },
  { value: CheckType.CLOUD_SECURITY, label: '云安全基线' },
  { value: CheckType.CUSTOM, label: '自定义基线' }
]

// 检查状态选项
const checkStatuses = [
  { value: CheckStatus.PENDING, label: '待检查' },
  { value: CheckStatus.RUNNING, label: '检查中' },
  { value: CheckStatus.COMPLETED, label: '已完成' },
  { value: CheckStatus.FAILED, label: '检查失败' },
  { value: CheckStatus.CANCELLED, label: '已取消' }
]

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    
    const response = await searchBaselineChecks(params)
    if (response.code === 200) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    } else {
      Message.error(response.message || '获取基线检查列表失败')
    }
  } catch (error) {
    console.error('获取基线检查列表失败:', error)
    Message.error('获取基线检查列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await getCheckStatistics()
    if (response.code === 200) {
      const data = response.data
      statistics.totalChecks = Object.values(data.statusCounts).reduce((a: number, b: number) => a + b, 0)
      statistics.runningChecks = data.statusCounts[CheckStatus.RUNNING] || 0
      statistics.completedChecks = data.statusCounts[CheckStatus.COMPLETED] || 0
      statistics.averageScore = data.averageComplianceScore || 0
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const handleSearch = () => {
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

const refreshData = () => {
  loadData()
  loadStatistics()
}

const handleCreateCheck = () => {
  // 暂时显示提示，后续实现创建功能
  Message.info('创建基线检查功能开发中...')
}

const viewDetails = (record: BaselineCheck) => {
  Message.info('查看详情功能开发中...')
}

const viewReport = (record: BaselineCheck) => {
  Message.info('查看报告功能开发中...')
}

const startCheck = async (record: BaselineCheck) => {
  try {
    const response = await startBaselineCheck(record.id!)
    if (response.code === 200) {
      Message.success('基线检查已启动')
      refreshData()
    } else {
      Message.error(response.message || '启动基线检查失败')
    }
  } catch (error) {
    console.error('启动基线检查失败:', error)
    Message.error('启动基线检查失败')
  }
}

const stopCheck = async (record: BaselineCheck) => {
  try {
    const response = await stopBaselineCheck(record.id!)
    if (response.code === 200) {
      Message.success('基线检查已停止')
      refreshData()
    } else {
      Message.error(response.message || '停止基线检查失败')
    }
  } catch (error) {
    console.error('停止基线检查失败:', error)
    Message.error('停止基线检查失败')
  }
}

const deleteCheck = async (record: BaselineCheck) => {
  try {
    const response = await deleteBaselineCheck(record.id!)
    if (response.code === 200) {
      Message.success('基线检查已删除')
      refreshData()
    } else {
      Message.error(response.message || '删除基线检查失败')
    }
  } catch (error) {
    console.error('删除基线检查失败:', error)
    Message.error('删除基线检查失败')
  }
}

// 工具方法
const getCheckTypeColor = (type: CheckType) => {
  const colors = {
    [CheckType.SYSTEM_SECURITY]: 'blue',
    [CheckType.NETWORK_SECURITY]: 'green',
    [CheckType.DATABASE_SECURITY]: 'purple',
    [CheckType.WEB_SECURITY]: 'orange',
    [CheckType.MIDDLEWARE_SECURITY]: 'cyan',
    [CheckType.CLOUD_SECURITY]: 'magenta',
    [CheckType.CUSTOM]: 'gray'
  }
  return colors[type] || 'gray'
}

const getCheckTypeLabel = (type: CheckType) => {
  const labels = {
    [CheckType.SYSTEM_SECURITY]: '系统安全',
    [CheckType.NETWORK_SECURITY]: '网络安全',
    [CheckType.DATABASE_SECURITY]: '数据库安全',
    [CheckType.WEB_SECURITY]: 'Web安全',
    [CheckType.MIDDLEWARE_SECURITY]: '中间件安全',
    [CheckType.CLOUD_SECURITY]: '云安全',
    [CheckType.CUSTOM]: '自定义'
  }
  return labels[type] || type
}

const getStatusColor = (status: CheckStatus) => {
  const colors = {
    [CheckStatus.PENDING]: 'gray',
    [CheckStatus.RUNNING]: 'blue',
    [CheckStatus.COMPLETED]: 'green',
    [CheckStatus.FAILED]: 'red',
    [CheckStatus.CANCELLED]: 'orange'
  }
  return colors[status] || 'gray'
}

const getStatusLabel = (status: CheckStatus) => {
  const labels = {
    [CheckStatus.PENDING]: '待检查',
    [CheckStatus.RUNNING]: '检查中',
    [CheckStatus.COMPLETED]: '已完成',
    [CheckStatus.FAILED]: '检查失败',
    [CheckStatus.CANCELLED]: '已取消'
  }
  return labels[status] || status
}

const getResultColor = (result: CheckResult) => {
  const colors = {
    [CheckResult.PASS]: 'green',
    [CheckResult.FAIL]: 'red',
    [CheckResult.WARNING]: 'orange',
    [CheckResult.PARTIAL]: 'blue'
  }
  return colors[result] || 'gray'
}

const getResultLabel = (result: CheckResult) => {
  const labels = {
    [CheckResult.PASS]: '通过',
    [CheckResult.FAIL]: '失败',
    [CheckResult.WARNING]: '警告',
    [CheckResult.PARTIAL]: '部分通过'
  }
  return labels[result] || result
}

const getProgressStatus = (status: CheckStatus) => {
  if (status === CheckStatus.RUNNING) return 'normal'
  if (status === CheckStatus.COMPLETED) return 'success'
  if (status === CheckStatus.FAILED) return 'danger'
  return 'normal'
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 50) return 'score-warning'
  return 'score-danger'
}

// 生命周期
onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.baseline-check-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 8px 0;
  color: #1d2129;
}

.page-description {
  color: #86909c;
  margin: 0;
}

.statistics-cards {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.action-left {
  display: flex;
  gap: 8px;
}

.action-right {
  display: flex;
  align-items: center;
}

.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.score-excellent {
  color: #00b42a;
  font-weight: 600;
}

.score-good {
  color: #ff7d00;
  font-weight: 600;
}

.score-warning {
  color: #ff7d00;
  font-weight: 600;
}

.score-danger {
  color: #f53f3f;
  font-weight: 600;
}
</style>
