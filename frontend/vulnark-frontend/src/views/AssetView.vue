<template>
  <div class="asset-view">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h1>资产管理</h1>
        <p>管理和监控所有IT资产</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <template #icon><icon-plus /></template>
            新建资产
          </a-button>
          <a-button @click="showImportModal">
            <template #icon><icon-upload /></template>
            批量导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><icon-download /></template>
            批量导出
          </a-button>
          <a-button @click="handleDetectAll">
            <template #icon><icon-refresh /></template>
            检测资产
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="总资产数" :value="stats.total" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="活跃资产" :value="stats.active" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="高风险" :value="stats.high + stats.critical" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic title="维护中" :value="stats.maintenance" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选 -->
    <a-card class="search-card">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="资产名称">
          <a-input v-model="searchForm.name" placeholder="请输入资产名称" allow-clear />
        </a-form-item>
        <a-form-item label="资产类型">
          <a-select v-model="searchForm.type" placeholder="请选择类型" allow-clear style="width: 150px">
            <a-option v-for="option in typeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="重要性">
          <a-select v-model="searchForm.importance" placeholder="请选择重要性" allow-clear style="width: 120px">
            <a-option v-for="option in importanceOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="IP地址">
          <a-input v-model="searchForm.ipAddress" placeholder="请输入IP地址" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 资产列表 -->
    <a-card class="table-card">
      <a-table 
        :columns="columns" 
        :data="assets" 
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #type="{ record }">
          <a-tag :color="getTypeColor(record.type)">
            {{ getTypeLabel(record.type) }}
          </a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template #importance="{ record }">
          <a-tag :color="getImportanceColor(record.importance)">
            {{ getImportanceLabel(record.importance) }}
          </a-tag>
        </template>
        <template #riskScore="{ record }">
          <a-progress 
            :percent="record.riskScore * 10" 
            :color="getRiskScoreColor(record.riskScore)"
            :size="'small'"
            :show-text="false"
          />
          <span style="margin-left: 8px;">{{ record.riskScore.toFixed(1) }}</span>
        </template>
        <template #lastScanTime="{ record }">
          <span v-if="record.lastScanTime">{{ formatDateTime(record.lastScanTime) }}</span>
          <span v-else class="text-gray">未扫描</span>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="showDetailModal(record)">详情</a-button>
            <a-button type="text" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-button type="text" size="small" @click="handleDetect(record)">检测</a-button>
            <a-button type="text" size="small" @click="showDetectionHistory(record)">检测历史</a-button>
            <a-button type="text" size="small" status="danger" @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑资产模态框 -->
    <a-modal 
      v-model:visible="modalVisible" 
      :title="modalTitle"
      width="900px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="资产名称" field="name">
              <a-input v-model="formData.name" placeholder="请输入资产名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="资产类型" field="type">
              <a-select v-model="formData.type" placeholder="请选择资产类型">
                <a-option v-for="option in typeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" field="status">
              <a-select v-model="formData.status" placeholder="请选择状态">
                <a-option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="重要性" field="importance">
              <a-select v-model="formData.importance" placeholder="请选择重要性">
                <a-option v-for="option in importanceOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="所属项目" field="projectId">
              <a-select v-model="formData.projectId" placeholder="请选择项目">
                <a-option v-for="project in projects" :key="project.id" :value="project.id">
                  {{ project.name }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="IP地址" field="ipAddress">
              <a-input v-model="formData.ipAddress" placeholder="请输入IP地址" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="域名" field="domain">
              <a-input v-model="formData.domain" placeholder="请输入域名" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="端口" field="port">
              <a-input-number v-model="formData.port" :min="1" :max="65535" placeholder="请输入端口" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="协议" field="protocol">
              <a-input v-model="formData.protocol" placeholder="请输入协议" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="服务" field="service">
              <a-input v-model="formData.service" placeholder="请输入服务" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="版本" field="version">
              <a-input v-model="formData.version" placeholder="请输入版本" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="操作系统" field="operatingSystem">
              <a-input v-model="formData.operatingSystem" placeholder="请输入操作系统" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="位置" field="location">
              <a-input v-model="formData.location" placeholder="请输入位置" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="供应商" field="vendor">
              <a-input v-model="formData.vendor" placeholder="请输入供应商" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="风险评分" field="riskScore">
              <a-input-number v-model="formData.riskScore" :min="0" :max="10" :precision="1" placeholder="请输入风险评分" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="资产描述" field="description">
          <a-textarea v-model="formData.description" placeholder="请输入资产描述" :rows="3" />
        </a-form-item>
        <a-form-item label="资产标签" field="tags">
          <a-input v-model="formData.tags" placeholder="请输入资产标签，用逗号分隔" />
        </a-form-item>
        <a-form-item label="备注" field="notes">
          <a-textarea v-model="formData.notes" placeholder="请输入备注信息" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 资产详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="资产详情"
      width="1200px"
      :footer="false"
    >
      <a-tabs v-if="selectedAsset" default-active-key="basic">
        <a-tab-pane key="basic" title="基本信息">
          <a-descriptions :column="2" bordered>
        <a-descriptions-item label="资产名称">{{ selectedAsset.name }}</a-descriptions-item>
        <a-descriptions-item label="资产类型">
          <a-tag :color="getTypeColor(selectedAsset.type)">
            {{ getTypeLabel(selectedAsset.type) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(selectedAsset.status)">
            {{ getStatusLabel(selectedAsset.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="重要性">
          <a-tag :color="getImportanceColor(selectedAsset.importance)">
            {{ getImportanceLabel(selectedAsset.importance) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ selectedAsset.ipAddress || '-' }}</a-descriptions-item>
        <a-descriptions-item label="域名">{{ selectedAsset.domain || '-' }}</a-descriptions-item>
        <a-descriptions-item label="端口">{{ selectedAsset.port || '-' }}</a-descriptions-item>
        <a-descriptions-item label="协议">{{ selectedAsset.protocol || '-' }}</a-descriptions-item>
        <a-descriptions-item label="服务">{{ selectedAsset.service || '-' }}</a-descriptions-item>
        <a-descriptions-item label="版本">{{ selectedAsset.version || '-' }}</a-descriptions-item>
        <a-descriptions-item label="操作系统">{{ selectedAsset.operatingSystem || '-' }}</a-descriptions-item>
        <a-descriptions-item label="位置">{{ selectedAsset.location || '-' }}</a-descriptions-item>
        <a-descriptions-item label="供应商">{{ selectedAsset.vendor || '-' }}</a-descriptions-item>
        <a-descriptions-item label="风险评分">
          <a-progress
            :percent="selectedAsset.riskScore * 10"
            :color="getRiskScoreColor(selectedAsset.riskScore)"
            :size="'small'"
          />
          {{ selectedAsset.riskScore.toFixed(1) }}
        </a-descriptions-item>
        <a-descriptions-item label="漏洞数量">{{ selectedAsset.vulnerabilityCount }}</a-descriptions-item>
        <a-descriptions-item label="最后扫描时间">
          <span v-if="selectedAsset.lastScanTime">{{ formatDateTime(selectedAsset.lastScanTime) }}</span>
          <span v-else class="text-gray">未扫描</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDateTime(selectedAsset.createdTime) }}</a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ formatDateTime(selectedAsset.updatedTime) }}</a-descriptions-item>
            <a-descriptions-item label="资产描述" :span="2">{{ selectedAsset.description || '-' }}</a-descriptions-item>
            <a-descriptions-item label="资产标签" :span="2">{{ selectedAsset.tags || '-' }}</a-descriptions-item>
            <a-descriptions-item label="备注" :span="2">{{ selectedAsset.notes || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="detection" title="检测状态">
          <div class="detection-section">
            <div class="section-header">
              <h3>最新检测状态</h3>
              <a-button type="primary" size="small" @click="handleDetect(selectedAsset)" :loading="detectingAssets.has(selectedAsset.id)">
                <template #icon><icon-refresh /></template>
                重新检测
              </a-button>
            </div>
            <a-table
              :columns="detectionColumns"
              :data="latestDetections"
              :pagination="false"
              size="small"
            >
              <template #type="{ record }">
                <a-tag>{{ getDetectionTypeLabel(record.type) }}</a-tag>
              </template>
              <template #status="{ record }">
                <a-tag :color="getDetectionStatusColor(record.status)">
                  {{ getDetectionStatusLabel(record.status) }}
                </a-tag>
              </template>
              <template #result="{ record }">
                <a-tag v-if="record.result" :color="getDetectionResultColor(record.result)">
                  {{ getDetectionResultLabel(record.result) }}
                </a-tag>
                <span v-else>-</span>
              </template>
              <template #responseTime="{ record }">
                {{ formatResponseTime(record.responseTime) }}
              </template>
            </a-table>
          </div>
        </a-tab-pane>

        <a-tab-pane key="fingerprint" title="指纹信息">
          <div class="fingerprint-section">
            <div class="section-header">
              <h3>技术栈指纹</h3>
              <a-button type="primary" size="small" @click="refreshFingerprints(selectedAsset.id)">
                <template #icon><icon-refresh /></template>
                刷新指纹
              </a-button>
            </div>
            <a-table
              :columns="fingerprintColumns"
              :data="assetFingerprints"
              :pagination="false"
              size="small"
            >
              <template #type="{ record }">
                <a-tag>{{ getFingerprintTypeLabel(record.type) }}</a-tag>
              </template>
              <template #name="{ record }">
                <strong>{{ record.name }}</strong>
                <span v-if="record.version" class="version-info"> v{{ record.version }}</span>
              </template>
              <template #vendor="{ record }">
                {{ record.vendor || '-' }}
              </template>
              <template #confidence="{ record }">
                <a-progress
                  :percent="record.confidence"
                  :color="getConfidenceColor(record.confidence)"
                  size="small"
                />
                {{ record.confidence }}%
              </template>
              <template #method="{ record }">
                <a-tag size="small">{{ record.method }}</a-tag>
              </template>
            </a-table>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-modal>

    <!-- 批量导入模态框 -->
    <a-modal
      v-model:visible="importModalVisible"
      title="批量导入资产"
      @ok="handleImport"
      @cancel="importModalVisible = false"
    >
      <a-upload
        :custom-request="handleFileUpload"
        :show-file-list="false"
        accept=".json,.csv,.xlsx"
      >
        <template #upload-button>
          <div class="upload-area">
            <icon-upload size="48" />
            <div>点击上传文件</div>
            <div class="upload-tip">支持 JSON、CSV、Excel 格式</div>
          </div>
        </template>
      </a-upload>
      <div v-if="importData.length > 0" class="import-preview">
        <h4>导入预览 ({{ importData.length }} 条记录)</h4>
        <a-table :columns="importColumns" :data="importData" :pagination="false" size="small" />
      </div>
    </a-modal>

    <!-- 检测历史模态框 -->
    <a-modal
      v-model:visible="detectionHistoryVisible"
      title="检测历史"
      width="1000px"
      :footer="false"
    >
      <a-table
        :columns="detectionHistoryColumns"
        :data="detectionHistory"
        :loading="loadingHistory"
        :pagination="historyPagination"
        @page-change="handleHistoryPageChange"
      >
        <template #type="{ record }">
          <a-tag>{{ getDetectionTypeLabel(record.type) }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="getDetectionStatusColor(record.status)">
            {{ getDetectionStatusLabel(record.status) }}
          </a-tag>
        </template>
        <template #result="{ record }">
          <a-tag v-if="record.result" :color="getDetectionResultColor(record.result)">
            {{ getDetectionResultLabel(record.result) }}
          </a-tag>
          <span v-else>-</span>
        </template>
        <template #responseTime="{ record }">
          {{ formatResponseTime(record.responseTime) }}
        </template>
        <template #createdTime="{ record }">
          {{ formatDateTime(record.createdTime) }}
        </template>
        <template #details="{ record }">
          <a-tooltip :content="record.details || record.errorMessage">
            <span class="details-text">{{ (record.details || record.errorMessage || '').substring(0, 30) }}...</span>
          </a-tooltip>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconPlus, IconUpload, IconDownload, IconRefresh } from '@arco-design/web-vue/es/icon'
import { formatDateTime as formatDateTimeUtil } from '@/utils/date'
import {
  getAssets,
  createAsset,
  updateAsset,
  deleteAsset,
  updateAssetScanTime,
  getAssetStats,
  importAssets,
  exportAssets,
  type Asset,
  type AssetRequest,
  type AssetQueryParams,
  type AssetStats
} from '@/api/asset'
import { getAllProjects, type Project } from '@/api/project'
import {
  assetDetectionApi,
  detectionUtils,
  type AssetDetection,
  type AssetFingerprint,
  type DetectionResult,
  type DetectionType,
  type DetectionStatus,
  type DetectionResult as DetectionResultType,
  type FingerprintType
} from '@/api/assetDetection'

// 响应式数据
const assets = ref<Asset[]>([])
const projects = ref<Project[]>([])
const stats = ref<AssetStats>({
  total: 0,
  active: 0,
  inactive: 0,
  maintenance: 0,
  high: 0,
  critical: 0
})
const loading = ref(false)
const modalVisible = ref(false)
const detailModalVisible = ref(false)
const importModalVisible = ref(false)
const selectedAsset = ref<Asset | null>(null)
const editingId = ref<number | null>(null)
const importData = ref<any[]>([])

// 检测相关状态
const detectionHistoryVisible = ref(false)
const latestDetections = ref<AssetDetection[]>([])
const assetFingerprints = ref<AssetFingerprint[]>([])
const detectionHistory = ref<AssetDetection[]>([])
const loadingHistory = ref(false)
const detectingAssets = ref(new Set<number>())

// 搜索表单
const searchForm = reactive<AssetQueryParams>({
  name: '',
  type: undefined,
  status: undefined,
  importance: undefined,
  projectId: undefined,
  ipAddress: '',
  keyword: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true
})

// 检测历史分页配置
const historyPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true
})

// 表单数据
const formData = reactive<AssetRequest>({
  name: '',
  description: '',
  type: 'SERVER',
  status: 'ACTIVE',
  ipAddress: '',
  domain: '',
  port: undefined,
  protocol: '',
  service: '',
  version: '',
  operatingSystem: '',
  importance: 'MEDIUM',
  projectId: 1,
  ownerId: undefined,
  location: '',
  vendor: '',
  tags: '',
  riskScore: 0.0,
  notes: ''
})

// 表单引用
const formRef = ref()

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入资产名称' }],
  type: [{ required: true, message: '请选择资产类型' }],
  projectId: [{ required: true, message: '请选择所属项目' }]
}

// 资产类型选项
const typeOptions = [
  { value: 'SERVER', label: '服务器' },
  { value: 'WORKSTATION', label: '工作站' },
  { value: 'NETWORK_DEVICE', label: '网络设备' },
  { value: 'DATABASE', label: '数据库' },
  { value: 'WEB_APPLICATION', label: 'Web应用' },
  { value: 'MOBILE_APPLICATION', label: '移动应用' },
  { value: 'IOT_DEVICE', label: 'IoT设备' },
  { value: 'CLOUD_SERVICE', label: '云服务' },
  { value: 'OTHER', label: '其他' }
]

// 状态选项
const statusOptions = [
  { value: 'ACTIVE', label: '活跃' },
  { value: 'INACTIVE', label: '非活跃' },
  { value: 'MAINTENANCE', label: '维护中' },
  { value: 'DECOMMISSIONED', label: '已停用' }
]

// 重要性选项
const importanceOptions = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '紧急' }
]

// 表格列配置
const columns = [
  { title: '资产名称', dataIndex: 'name', width: 150 },
  { title: '类型', slotName: 'type', width: 120 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '重要性', slotName: 'importance', width: 100 },
  { title: 'IP地址', dataIndex: 'ipAddress', width: 120 },
  { title: '域名', dataIndex: 'domain', width: 150 },
  { title: '服务', dataIndex: 'service', width: 100 },
  { title: '风险评分', slotName: 'riskScore', width: 120 },
  { title: '漏洞数', dataIndex: 'vulnerabilityCount', width: 80 },
  { title: '最后扫描', slotName: 'lastScanTime', width: 140 },
  { title: '操作', slotName: 'actions', width: 200, fixed: 'right' }
]

// 导入预览列配置
const importColumns = [
  { title: '资产名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'type' },
  { title: 'IP地址', dataIndex: 'ipAddress' },
  { title: '状态', dataIndex: 'status' }
]

// 检测状态表格列配置
const detectionColumns = [
  { title: '检测类型', slotName: 'type', width: 120 },
  { title: '目标', dataIndex: 'target', width: 150 },
  { title: '端口', dataIndex: 'port', width: 80 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '结果', slotName: 'result', width: 100 },
  { title: '响应时间', slotName: 'responseTime', width: 100 },
  { title: '检测时间', dataIndex: 'createdTime', width: 140 }
]

// 指纹信息表格列配置
const fingerprintColumns = [
  { title: '类型', slotName: 'type', width: 120 },
  { title: '技术/产品', slotName: 'name', width: 200 },
  { title: '厂商', slotName: 'vendor', width: 120 },
  { title: '置信度', slotName: 'confidence', width: 100 },
  { title: '识别方法', slotName: 'method', width: 120 },
  { title: '端口', dataIndex: 'port', width: 80 }
]

// 检测历史表格列配置
const detectionHistoryColumns = [
  { title: '检测类型', slotName: 'type', width: 120 },
  { title: '目标', dataIndex: 'target', width: 150 },
  { title: '端口', dataIndex: 'port', width: 80 },
  { title: '状态', slotName: 'status', width: 100 },
  { title: '结果', slotName: 'result', width: 100 },
  { title: '响应时间', slotName: 'responseTime', width: 100 },
  { title: '检测时间', slotName: 'createdTime', width: 140 },
  { title: '详情', slotName: 'details', width: 200 }
]

// 计算属性
const modalTitle = computed(() => editingId.value ? '编辑资产' : '新建资产')

// 方法
const loadAssets = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    const response = await getAssets(params)
    assets.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    Message.error('获取资产列表失败')
  } finally {
    loading.value = false
  }
}

const loadProjects = async () => {
  try {
    const response = await getAllProjects()
    projects.value = response.data
  } catch (error) {
    Message.error('获取项目列表失败')
  }
}

const loadStats = async () => {
  try {
    const response = await getAssetStats()
    stats.value = response.data
  } catch (error) {
    Message.error('获取统计信息失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadAssets()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: undefined,
    status: undefined,
    importance: undefined,
    projectId: undefined,
    ipAddress: '',
    keyword: ''
  })
  pagination.current = 1
  loadAssets()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadAssets()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  loadAssets()
}

const showCreateModal = () => {
  editingId.value = null
  resetFormData()
  modalVisible.value = true
}

const showEditModal = (asset: Asset) => {
  editingId.value = asset.id
  Object.assign(formData, {
    name: asset.name,
    description: asset.description,
    type: asset.type,
    status: asset.status,
    ipAddress: asset.ipAddress,
    domain: asset.domain,
    port: asset.port,
    protocol: asset.protocol,
    service: asset.service,
    version: asset.version,
    operatingSystem: asset.operatingSystem,
    importance: asset.importance,
    projectId: asset.projectId,
    ownerId: asset.ownerId,
    location: asset.location,
    vendor: asset.vendor,
    tags: asset.tags,
    riskScore: asset.riskScore,
    notes: asset.notes
  })
  modalVisible.value = true
}



const showImportModal = () => {
  importData.value = []
  importModalVisible.value = true
}

const handleModalOk = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return

    if (editingId.value) {
      await updateAsset(editingId.value, formData)
      Message.success('资产更新成功')
    } else {
      await createAsset(formData)
      Message.success('资产创建成功')
    }

    modalVisible.value = false
    loadAssets()
    loadStats()
  } catch (error) {
    Message.error('操作失败')
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetFormData()
}

const handleDelete = (asset: Asset) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除资产"${asset.name}"吗？此操作不可恢复。`,
    onOk: async () => {
      try {
        await deleteAsset(asset.id)
        Message.success('资产删除成功')
        loadAssets()
        loadStats()
      } catch (error) {
        Message.error('删除失败')
      }
    }
  })
}

const handleScan = async (asset: Asset) => {
  try {
    await updateAssetScanTime(asset.id)
    Message.success('扫描完成')
    loadAssets()
  } catch (error) {
    Message.error('扫描失败')
  }
}

const handleScanAll = () => {
  Modal.confirm({
    title: '确认扫描',
    content: '确定要扫描所有活跃资产吗？这可能需要一些时间。',
    onOk: async () => {
      try {
        // 这里可以调用批量扫描API
        Message.success('批量扫描已启动')
      } catch (error) {
        Message.error('批量扫描失败')
      }
    }
  })
}

const handleExport = async () => {
  try {
    const response = await exportAssets()
    // 创建下载链接
    const blob = new Blob([JSON.stringify(response, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `assets_${new Date().toISOString().split('T')[0]}.json`
    link.click()
    URL.revokeObjectURL(url)
    Message.success('导出成功')
  } catch (error) {
    Message.error('导出失败')
  }
}

const handleFileUpload = (option: any) => {
  const file = option.fileItem.file
  const reader = new FileReader()

  reader.onload = (e) => {
    try {
      const content = e.target?.result as string
      let data: any[] = []

      if (file.name.endsWith('.json')) {
        data = JSON.parse(content)
      } else if (file.name.endsWith('.csv')) {
        // 简单的CSV解析
        const lines = content.split('\n')
        const headers = lines[0].split(',')
        data = lines.slice(1).map(line => {
          const values = line.split(',')
          const obj: any = {}
          headers.forEach((header, index) => {
            obj[header.trim()] = values[index]?.trim()
          })
          return obj
        })
      }

      importData.value = data
      Message.success('文件解析成功')
    } catch (error) {
      Message.error('文件解析失败')
    }
  }

  reader.readAsText(file)
}

const handleImport = async () => {
  try {
    if (importData.value.length === 0) {
      Message.warning('请先上传文件')
      return
    }

    await importAssets(importData.value)
    Message.success('导入成功')
    importModalVisible.value = false
    loadAssets()
    loadStats()
  } catch (error) {
    Message.error('导入失败')
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    type: 'SERVER',
    status: 'ACTIVE',
    ipAddress: '',
    domain: '',
    port: undefined,
    protocol: '',
    service: '',
    version: '',
    operatingSystem: '',
    importance: 'MEDIUM',
    projectId: 1,
    ownerId: undefined,
    location: '',
    vendor: '',
    tags: '',
    riskScore: 0.0,
    notes: ''
  })
}

// 辅助方法
const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    SERVER: 'blue',
    WORKSTATION: 'green',
    NETWORK_DEVICE: 'orange',
    DATABASE: 'purple',
    WEB_APPLICATION: 'cyan',
    MOBILE_APPLICATION: 'magenta',
    IOT_DEVICE: 'lime',
    CLOUD_SERVICE: 'gold',
    OTHER: 'gray'
  }
  return colors[type] || 'gray'
}

const getTypeLabel = (type: string) => {
  const option = typeOptions.find(opt => opt.value === type)
  return option?.label || type
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    ACTIVE: 'green',
    INACTIVE: 'gray',
    MAINTENANCE: 'orange',
    DECOMMISSIONED: 'red'
  }
  return colors[status] || 'gray'
}

const getStatusLabel = (status: string) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option?.label || status
}

const getImportanceColor = (importance: string) => {
  const colors: Record<string, string> = {
    LOW: 'green',
    MEDIUM: 'blue',
    HIGH: 'orange',
    CRITICAL: 'red'
  }
  return colors[importance] || 'gray'
}

const getImportanceLabel = (importance: string) => {
  const option = importanceOptions.find(opt => opt.value === importance)
  return option?.label || importance
}

const getRiskScoreColor = (score: number) => {
  if (score >= 8) return '#f5222d'
  if (score >= 6) return '#fa8c16'
  if (score >= 4) return '#fadb14'
  return '#52c41a'
}

const formatDateTime = (dateTime: string | null | undefined) => {
  return formatDateTimeUtil(dateTime, 'YYYY-MM-DD HH:mm')
}

// 检测相关方法
const handleDetect = async (asset: Asset) => {
  try {
    detectingAssets.value.add(asset.id)
    await assetDetectionApi.detectAssetAsync(asset.id, true)
    Message.success('检测任务已启动，请稍后查看结果')

    // 延迟刷新检测状态
    setTimeout(() => {
      if (selectedAsset.value?.id === asset.id) {
        loadLatestDetections(asset.id)
        loadAssetFingerprints(asset.id)
      }
    }, 3000)
  } catch (error) {
    Message.error('启动检测失败')
  } finally {
    detectingAssets.value.delete(asset.id)
  }
}

const handleDetectAll = async () => {
  try {
    const assetIds = assets.value.map(asset => asset.id)
    await assetDetectionApi.detectAssetsAsync(assetIds, true)
    Message.success(`已启动 ${assetIds.length} 个资产的检测任务`)
  } catch (error) {
    Message.error('启动批量检测失败')
  }
}

const showDetectionHistory = async (asset: Asset) => {
  selectedAsset.value = asset
  detectionHistoryVisible.value = true
  await loadDetectionHistory(asset.id)
}

const loadLatestDetections = async (assetId: number) => {
  try {
    latestDetections.value = await assetDetectionApi.getLatestDetections(assetId)
  } catch (error) {
    console.error('加载最新检测状态失败:', error)
  }
}

const loadAssetFingerprints = async (assetId: number) => {
  try {
    assetFingerprints.value = await assetDetectionApi.getFingerprints(assetId)
  } catch (error) {
    console.error('加载资产指纹失败:', error)
  }
}

const loadDetectionHistory = async (assetId: number) => {
  try {
    loadingHistory.value = true
    const result = await assetDetectionApi.getDetectionHistory(
      assetId,
      historyPagination.current - 1,
      historyPagination.pageSize
    )
    detectionHistory.value = result.content
    historyPagination.total = result.totalElements
  } catch (error) {
    Message.error('加载检测历史失败')
  } finally {
    loadingHistory.value = false
  }
}

const refreshFingerprints = async (assetId: number) => {
  await loadAssetFingerprints(assetId)
  Message.success('指纹信息已刷新')
}

const handleHistoryPageChange = (page: number) => {
  historyPagination.current = page
  if (selectedAsset.value) {
    loadDetectionHistory(selectedAsset.value.id)
  }
}

// 检测相关工具方法
const getDetectionTypeLabel = (type: DetectionType) => {
  return detectionUtils.getDetectionTypeLabel(type)
}

const getDetectionStatusLabel = (status: DetectionStatus) => {
  return detectionUtils.getDetectionStatusLabel(status)
}

const getDetectionStatusColor = (status: DetectionStatus) => {
  const colors = {
    PENDING: 'blue',
    RUNNING: 'orange',
    COMPLETED: 'green',
    FAILED: 'red',
    TIMEOUT: 'orange',
    CANCELLED: 'gray'
  }
  return colors[status] || 'gray'
}

const getDetectionResultLabel = (result: DetectionResultType) => {
  return detectionUtils.getDetectionResultLabel(result)
}

const getDetectionResultColor = (result: DetectionResultType) => {
  return detectionUtils.getDetectionResultColor(result)
}

const getFingerprintTypeLabel = (type: FingerprintType) => {
  return detectionUtils.getFingerprintTypeLabel(type)
}

const getConfidenceColor = (confidence: number) => {
  return detectionUtils.getConfidenceColor(confidence)
}

const formatResponseTime = (responseTime?: number) => {
  return detectionUtils.formatResponseTime(responseTime)
}

// 重写showDetailModal方法，加载检测信息
const showDetailModal = async (asset: Asset) => {
  selectedAsset.value = asset
  detailModalVisible.value = true

  // 加载检测状态和指纹信息
  await Promise.all([
    loadLatestDetections(asset.id),
    loadAssetFingerprints(asset.id)
  ])
}

// 生命周期
onMounted(() => {
  loadAssets()
  loadProjects()
  loadStats()
})
</script>

<style scoped>
.asset-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-left p {
  margin: 5px 0 0 0;
  color: #666;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.text-gray {
  color: #999;
}

.upload-area {
  text-align: center;
  padding: 40px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #1890ff;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.import-preview {
  margin-top: 20px;
}

.import-preview h4 {
  margin-bottom: 10px;
}

.detection-section, .fingerprint-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.version-info {
  color: #666;
  font-size: 12px;
}

.details-text {
  cursor: pointer;
  color: #666;
}
</style>
