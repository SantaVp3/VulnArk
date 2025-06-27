<template>
  <div class="asset-dependency-view">
    <!-- 页面头部 -->
    <div class="page-header page-header-enhanced">
      <div class="header-left">
        <div class="header-title">
          <icon-relation class="title-icon" />
          <h1 class="gradient-text">资产依赖关系</h1>
        </div>
        <p class="header-subtitle">管理和可视化资产间的依赖关系</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="showCreateModal" class="btn-enhanced">
            <template #icon><icon-plus /></template>
            新建依赖关系
          </a-button>
          <a-button @click="showTopologyModal" class="btn-enhanced">
            <template #icon><icon-desktop /></template>
            拓扑图
          </a-button>
          <a-button @click="detectCircularDeps" class="btn-enhanced">
            <template #icon><icon-sync /></template>
            检测循环依赖
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards stats-cards-enhanced">
      <a-row :gutter="24">
        <a-col :span="6">
          <div class="stat-card-enhanced">
            <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff, #36cfc9);">
              <icon-relation />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalDependencies }}</div>
              <div class="stat-title">总依赖关系</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ff4d4f, #ff7875);">
              <icon-exclamation />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.criticalDependencies }}</div>
              <div class="stat-title">关键依赖</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced">
            <div class="stat-icon" style="background: linear-gradient(135deg, #faad14, #ffc53d);">
              <icon-close />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.brokenDependencies }}</div>
              <div class="stat-title">断开依赖</div>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card-enhanced">
            <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a, #73d13d);">
              <icon-check />
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ circularDependencies.length }}</div>
              <div class="stat-title">循环依赖</div>
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
        <a-form-item label="依赖类型">
          <a-select v-model="searchForm.dependencyType" placeholder="请选择依赖类型" allow-clear style="width: 150px">
            <a-option v-for="option in dependencyTypeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="依赖强度">
          <a-select v-model="searchForm.dependencyStrength" placeholder="请选择依赖强度" allow-clear style="width: 120px">
            <a-option v-for="option in dependencyStrengthOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-option v-for="option in dependencyStatusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键依赖">
          <a-select v-model="searchForm.isCritical" placeholder="是否关键" allow-clear style="width: 100px">
            <a-option :value="true">是</a-option>
            <a-option :value="false">否</a-option>
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

    <!-- 依赖关系列表 -->
    <div class="table-card-enhanced">
      <div class="table-header">
        <div class="table-title">
          <icon-list class="table-title-icon" />
          依赖关系列表
        </div>
        <div class="table-info">
          共 {{ pagination.total }} 个依赖关系
        </div>
      </div>
      <a-table 
        :columns="columns" 
        :data="dependencies" 
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        class="table-enhanced"
        row-key="id"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @selection-change="handleSelectionChange"
      >
        <template #sourceAsset="{ record }">
          <a-tag color="blue">{{ record.sourceAssetName || `资产${record.sourceAssetId}` }}</a-tag>
        </template>
        <template #targetAsset="{ record }">
          <a-tag color="green">{{ record.targetAssetName || `资产${record.targetAssetId}` }}</a-tag>
        </template>
        <template #dependencyType="{ record }">
          <a-tag :color="getDependencyTypeColor(record.dependencyType)">
            {{ record.dependencyTypeDescription }}
          </a-tag>
        </template>
        <template #dependencyStrength="{ record }">
          <a-tag :color="getDependencyStrengthColor(record.dependencyStrength)">
            {{ record.dependencyStrengthDescription }}
          </a-tag>
        </template>
        <template #isCritical="{ record }">
          <a-tag :color="record.isCritical ? 'red' : 'gray'">
            {{ record.isCritical ? '是' : '否' }}
          </a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="getDependencyStatusColor(record.status)">
            {{ record.statusDescription }}
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

    <!-- 批量操作栏 -->
    <div v-if="selectedRows.length > 0" class="batch-actions">
      <a-space>
        <span>已选择 {{ selectedRows.length }} 项</span>
        <a-button type="primary" status="danger" @click="handleBatchDelete">
          <icon-delete />
          批量删除
        </a-button>
      </a-space>
    </div>

    <!-- 创建/编辑依赖关系模态框 -->
    <a-modal 
      v-model:visible="modalVisible" 
      :title="modalTitle"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="源资产" field="sourceAssetId">
              <a-select v-model="formData.sourceAssetId" placeholder="请选择源资产" allow-search>
                <a-option v-for="asset in assets" :key="asset.id" :value="asset.id">
                  {{ asset.name }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="目标资产" field="targetAssetId">
              <a-select v-model="formData.targetAssetId" placeholder="请选择目标资产" allow-search>
                <a-option v-for="asset in assets" :key="asset.id" :value="asset.id">
                  {{ asset.name }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="依赖类型" field="dependencyType">
              <a-select v-model="formData.dependencyType" placeholder="请选择依赖类型">
                <a-option v-for="option in dependencyTypeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="依赖强度" field="dependencyStrength">
              <a-select v-model="formData.dependencyStrength" placeholder="请选择依赖强度">
                <a-option v-for="option in dependencyStrengthOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="依赖描述" field="description">
          <a-textarea v-model="formData.description" placeholder="请输入依赖描述" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="端口" field="port">
              <a-input-number v-model="formData.port" placeholder="端口号" :min="1" :max="65535" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="协议" field="protocol">
              <a-input v-model="formData.protocol" placeholder="协议" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="服务名称" field="serviceName">
              <a-input v-model="formData.serviceName" placeholder="服务名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="是否关键依赖" field="isCritical">
              <a-switch v-model="formData.isCritical" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" field="status">
              <a-select v-model="formData.status" placeholder="请选择状态">
                <a-option v-for="option in dependencyStatusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 依赖关系详情模态框 -->
    <a-modal 
      v-model:visible="detailModalVisible" 
      title="依赖关系详情"
      width="600px"
      :footer="false"
    >
      <a-descriptions v-if="selectedDependency" :column="2" bordered>
        <a-descriptions-item label="源资产">{{ selectedDependency.sourceAssetName }}</a-descriptions-item>
        <a-descriptions-item label="目标资产">{{ selectedDependency.targetAssetName }}</a-descriptions-item>
        <a-descriptions-item label="依赖类型">{{ selectedDependency.dependencyTypeDescription }}</a-descriptions-item>
        <a-descriptions-item label="依赖强度">{{ selectedDependency.dependencyStrengthDescription }}</a-descriptions-item>
        <a-descriptions-item label="是否关键">{{ selectedDependency.isCritical ? '是' : '否' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ selectedDependency.statusDescription }}</a-descriptions-item>
        <a-descriptions-item label="端口">{{ selectedDependency.port || '-' }}</a-descriptions-item>
        <a-descriptions-item label="协议">{{ selectedDependency.protocol || '-' }}</a-descriptions-item>
        <a-descriptions-item label="服务名称">{{ selectedDependency.serviceName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ formatDateTime(selectedDependency.createdTime) }}</a-descriptions-item>
        <a-descriptions-item label="依赖描述" :span="2">{{ selectedDependency.description || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 拓扑图模态框 -->
    <a-modal 
      v-model:visible="topologyModalVisible" 
      title="资产依赖拓扑图"
      width="1200px"
      :footer="false"
    >
      <div class="topology-container">
        <div class="topology-toolbar">
          <a-space>
            <a-select v-model="selectedProjectId" placeholder="选择项目" style="width: 200px" @change="loadTopology">
              <a-option v-for="project in projects" :key="project.id" :value="project.id">
                {{ project.name }}
              </a-option>
            </a-select>
            <a-button @click="loadTopology">刷新</a-button>
          </a-space>
        </div>
        <div ref="topologyRef" class="topology-chart" style="height: 500px;"></div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconList,
  IconRelation,
  IconDesktop,
  IconSync,
  IconExclamation,
  IconClose,
  IconCheck,
  IconDelete
} from '@arco-design/web-vue/es/icon'
import { formatDateTime } from '@/utils/date'
import {
  createAssetDependency,
  updateAssetDependency,
  deleteAssetDependency,
  batchDeleteAssetDependencies,
  getAssetDependencyById,
  getProjectDependencyTopology,
  detectCircularDependencies,
  getDependencyStatistics,
  dependencyTypeOptions,
  dependencyStrengthOptions,
  dependencyStatusOptions,
  getDependencyTypeColor,
  getDependencyStrengthColor,
  getDependencyStatusColor,
  type AssetDependency,
  type AssetDependencyRequest,
  type AssetDependencyTopology,
  type DependencyStatistics
} from '@/api/assetDependency'
import { getAllAssets, type Asset } from '@/api/asset'
import { getAllProjects, type Project } from '@/api/project'

// 响应式数据
const dependencies = ref<AssetDependency[]>([])
const assets = ref<Asset[]>([])
const projects = ref<Project[]>([])
const statistics = ref<DependencyStatistics>({
  totalDependencies: 0,
  criticalDependencies: 0,
  brokenDependencies: 0,
  dependencyTypeStats: {},
  dependencyStrengthStats: {}
})
const circularDependencies = ref<AssetDependency[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const detailModalVisible = ref(false)
const topologyModalVisible = ref(false)
const selectedDependency = ref<AssetDependency | null>(null)
const selectedRows = ref<AssetDependency[]>([])
const editingId = ref<number | null>(null)
const selectedProjectId = ref<number | null>(null)
const topologyRef = ref()

// 搜索表单
const searchForm = reactive({
  dependencyType: undefined,
  dependencyStrength: undefined,
  status: undefined,
  isCritical: undefined
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
const formData = reactive<AssetDependencyRequest>({
  sourceAssetId: 0,
  targetAssetId: 0,
  dependencyType: 'NETWORK',
  dependencyStrength: 'MEDIUM',
  description: '',
  port: undefined,
  protocol: '',
  serviceName: '',
  isCritical: false,
  status: 'ACTIVE'
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
  sourceAssetId: [{ required: true, message: '请选择源资产' }],
  targetAssetId: [{ required: true, message: '请选择目标资产' }],
  dependencyType: [{ required: true, message: '请选择依赖类型' }],
  dependencyStrength: [{ required: true, message: '请选择依赖强度' }]
}

// 表格列配置
const columns = [
  { title: '源资产', slotName: 'sourceAsset', width: 150 },
  { title: '目标资产', slotName: 'targetAsset', width: 150 },
  { title: '依赖类型', slotName: 'dependencyType', width: 120 },
  { title: '依赖强度', slotName: 'dependencyStrength', width: 100 },
  { title: '关键依赖', slotName: 'isCritical', width: 80 },
  { title: '状态', slotName: 'status', width: 80 },
  { title: '端口', dataIndex: 'port', width: 80 },
  { title: '协议', dataIndex: 'protocol', width: 80 },
  { title: '创建时间', dataIndex: 'createdTime', width: 150, render: ({ record }) => formatDateTime(record.createdTime) },
  { title: '操作', slotName: 'actions', width: 150, fixed: 'right' }
]

// 计算属性
const modalTitle = computed(() => editingId.value ? '编辑依赖关系' : '新建依赖关系')

// 方法
const loadDependencies = async () => {
  loading.value = true
  try {
    // 这里应该调用分页查询API，暂时使用模拟数据
    dependencies.value = []
    pagination.total = 0
  } catch (error) {
    console.error('加载依赖关系失败:', error)
    Message.error('加载依赖关系失败')
  } finally {
    loading.value = false
  }
}

const loadAssets = async () => {
  try {
    const response = await getAllAssets()
    assets.value = response.data
  } catch (error) {
    console.error('加载资产列表失败:', error)
  }
}

const loadProjects = async () => {
  try {
    const response = await getAllProjects()
    projects.value = response.data
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

const loadStatistics = async () => {
  try {
    const response = await getDependencyStatistics()
    statistics.value = response.data
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const detectCircularDeps = async () => {
  try {
    const response = await detectCircularDependencies()
    circularDependencies.value = response.data
    if (circularDependencies.value.length > 0) {
      Message.warning(`检测到 ${circularDependencies.value.length} 个循环依赖`)
    } else {
      Message.success('未检测到循环依赖')
    }
  } catch (error) {
    console.error('检测循环依赖失败:', error)
    Message.error('检测循环依赖失败')
  }
}

const showCreateModal = () => {
  editingId.value = null
  resetFormData()
  modalVisible.value = true
}

const showEditModal = (dependency: AssetDependency) => {
  editingId.value = dependency.id
  Object.assign(formData, {
    sourceAssetId: dependency.sourceAssetId,
    targetAssetId: dependency.targetAssetId,
    dependencyType: dependency.dependencyType,
    dependencyStrength: dependency.dependencyStrength,
    description: dependency.description,
    port: dependency.port,
    protocol: dependency.protocol,
    serviceName: dependency.serviceName,
    isCritical: dependency.isCritical,
    status: dependency.status
  })
  modalVisible.value = true
}

const showDetailModal = (dependency: AssetDependency) => {
  selectedDependency.value = dependency
  detailModalVisible.value = true
}

const showTopologyModal = () => {
  topologyModalVisible.value = true
  if (projects.value.length > 0 && !selectedProjectId.value) {
    selectedProjectId.value = projects.value[0].id
  }
  nextTick(() => {
    loadTopology()
  })
}

const loadTopology = async () => {
  if (!selectedProjectId.value) return

  try {
    const response = await getProjectDependencyTopology(selectedProjectId.value)
    const topology = response.data
    renderTopology(topology)
  } catch (error) {
    console.error('加载拓扑图失败:', error)
    Message.error('加载拓扑图失败')
  }
}

const renderTopology = (topology: AssetDependencyTopology) => {
  // 这里应该使用图形库（如 ECharts、D3.js 等）来渲染拓扑图
  // 暂时只是占位实现
  console.log('渲染拓扑图:', topology)
}

const handleSearch = () => {
  pagination.current = 1
  loadDependencies()
}

const handleReset = () => {
  Object.assign(searchForm, {
    dependencyType: undefined,
    dependencyStrength: undefined,
    status: undefined,
    isCritical: undefined
  })
  handleSearch()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadDependencies()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadDependencies()
}

const handleSelectionChange = (rowKeys: string[], rows: AssetDependency[]) => {
  selectedRows.value = rows
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()

    if (editingId.value) {
      await updateAssetDependency(editingId.value, formData)
      Message.success('更新依赖关系成功')
    } else {
      await createAssetDependency(formData)
      Message.success('创建依赖关系成功')
    }

    modalVisible.value = false
    loadDependencies()
    loadStatistics()
  } catch (error) {
    console.error('保存依赖关系失败:', error)
    Message.error('保存依赖关系失败')
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetFormData()
}

const handleDelete = (dependency: AssetDependency) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除依赖关系"${dependency.sourceAssetName} -> ${dependency.targetAssetName}"吗？`,
    onOk: async () => {
      try {
        await deleteAssetDependency(dependency.id)
        Message.success('删除成功')
        loadDependencies()
        loadStatistics()
      } catch (error) {
        console.error('删除失败:', error)
        Message.error('删除失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRows.value.length} 个依赖关系吗？`,
    onOk: async () => {
      try {
        const ids = selectedRows.value.map(row => row.id)
        await batchDeleteAssetDependencies(ids)
        Message.success('批量删除成功')
        selectedRows.value = []
        loadDependencies()
        loadStatistics()
      } catch (error) {
        console.error('批量删除失败:', error)
        Message.error('批量删除失败')
      }
    }
  })
}

const resetFormData = () => {
  Object.assign(formData, {
    sourceAssetId: 0,
    targetAssetId: 0,
    dependencyType: 'NETWORK',
    dependencyStrength: 'MEDIUM',
    description: '',
    port: undefined,
    protocol: '',
    serviceName: '',
    isCritical: false,
    status: 'ACTIVE'
  })
}

// 生命周期
onMounted(() => {
  loadDependencies()
  loadAssets()
  loadProjects()
  loadStatistics()
  detectCircularDeps()
})
</script>

<style scoped>
.asset-dependency-view {
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

.stats-cards-enhanced {
  margin-bottom: var(--spacing-xl);
}

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

.batch-actions {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--bg-primary);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
  z-index: 1000;
}

.topology-container {
  width: 100%;
}

.topology-toolbar {
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.topology-chart {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
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

  .batch-actions {
    left: var(--spacing-md);
    right: var(--spacing-md);
    transform: none;
  }
}
</style>
